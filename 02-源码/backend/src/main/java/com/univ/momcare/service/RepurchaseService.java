package com.univ.momcare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.entity.*;
import com.univ.momcare.mapper.*;
import com.univ.momcare.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 复购提醒（需求文档 5.5）。
 * 预计用完日期 = 上次购买日期 + 消耗天数：
 *   奶粉按日均用量估算（800g 约 31 天，900g 约 35 天，700g 约 27 天）
 *   纸尿裤按片数消耗估算（每包片数 ÷ 每日 6 片）
 * 计划发送时间 = 预计用完日期前 3 天 10:00；同一会员同一商品 7 天内最多一条。
 */
@Service
@RequiredArgsConstructor
public class RepurchaseService {

    private final RepurchaseTaskMapper repurchaseMapper;
    private final MallOrderMapper orderMapper;
    private final MallOrderItemMapper itemMapper;
    private final BabyGoodsMapper goodsMapper;
    private final MemberMapper memberMapper;
    private final CodeGenService codeGen;
    private final OperLogService operLogService;

    @Transactional
    public Map<String, Object> generate() {
        LocalDateTime now = LocalDateTime.now();
        // 只看已完成订单，按时间倒序，取每个「会员 + 商品」最近一次购买
        List<MallOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getStatus, "已完成").orderByDesc(MallOrder::getCreateTime));
        Map<String, Object[]> latest = new LinkedHashMap<>();
        for (MallOrder o : orders) {
            for (MallOrderItem it : itemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                    .eq(MallOrderItem::getOrderId, o.getId()))) {
                BabyGoods g = goodsMapper.selectById(it.getGoodsId());
                if (g == null || !("奶粉".equals(g.getGoodsType()) || "纸尿裤".equals(g.getGoodsType()))) {
                    continue;
                }
                String key = o.getMemberId() + "-" + g.getId();
                latest.putIfAbsent(key, new Object[]{o, g});
            }
        }

        int created = 0;
        int skipped = 0;
        for (Object[] pair : latest.values()) {
            MallOrder o = (MallOrder) pair[0];
            BabyGoods g = (BabyGoods) pair[1];
            LocalDate lastBuy = o.getCreateTime().toLocalDate();
            int consumeDays = consumeDays(g);
            LocalDate predictEnd = lastBuy.plusDays(consumeDays);
            LocalDate planDate = predictEnd.minusDays(3);

            // 同一会员同一商品 7 天内已有提醒则跳过
            long dup = repurchaseMapper.selectCount(new LambdaQueryWrapper<RepurchaseTask>()
                    .eq(RepurchaseTask::getMemberId, o.getMemberId())
                    .eq(RepurchaseTask::getGoodsId, g.getId())
                    .ge(RepurchaseTask::getCreateTime, now.minusDays(7)));
            if (dup > 0) {
                skipped++;
                continue;
            }
            // 已经过期太久的不再生成，避免制造无效待办
            if (planDate.isBefore(LocalDate.now().minusDays(30))) {
                skipped++;
                continue;
            }

            Member m = memberMapper.selectById(o.getMemberId());
            RepurchaseTask t = new RepurchaseTask();
            t.setRemindNo(codeGen.remindNo());
            t.setMemberId(o.getMemberId());
            t.setGoodsId(g.getId());
            t.setLastBuyDate(lastBuy);
            t.setPredictEndDate(predictEnd);
            t.setRemindChannel(m != null && m.getPointBalance() != null && m.getPointBalance() % 3 == 0
                    ? "站内消息" : "导购电话");
            t.setPlanTime(planDate.atTime(10, 0));
            t.setSendStatus(planDate.isBefore(LocalDate.now()) ? "已发送" : "待发送");
            t.setResponseResult("无响应");
            t.setOwnerId(9L);
            repurchaseMapper.insert(t);
            created++;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("created", created);
        out.put("skipped", skipped);
        out.put("total", created + skipped);
        operLogService.log("复购提醒台", "新增", null, "批量生成复购提醒 " + created + " 条，跳过 " + skipped + " 条");
        return out;
    }

    /** 消耗天数估算：奶粉按克数，纸尿裤按片数 */
    private int consumeDays(BabyGoods g) {
        String spec = g.getSpecDesc() == null ? "" : g.getSpecDesc();
        if ("奶粉".equals(g.getGoodsType())) {
            int grams = 800;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*g").matcher(spec);
            if (m.find()) {
                grams = Integer.parseInt(m.group(1));
            }
            return Math.max(7, Math.round(grams / 26.0f));
        } else {
            int pieces = 42;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*片").matcher(spec);
            if (m.find()) {
                pieces = Integer.parseInt(m.group(1));
            }
            return Math.max(3, pieces / 6);
        }
    }

    /** 登记跟进结果：已复购不再提醒，退订只允许站内消息 */
    @Transactional
    public RepurchaseTask follow(Long id, String response, String remark) {
        RepurchaseTask t = repurchaseMapper.selectById(id);
        if (t == null) {
            throw new BizException("提醒记录不存在");
        }
        if (response == null || !List.of("已复购", "咨询未购", "无响应", "退订").contains(response)) {
            throw new BizException("请选择有效的会员响应结果");
        }
        t.setResponseResult(response);
        t.setSendStatus("已发送");
        if ("退订".equals(response)) {
            t.setRemindChannel("站内消息");
        }
        t.setOwnerId(CurrentUser.id());
        t.setUpdateBy(CurrentUser.id());
        repurchaseMapper.updateById(t);
        operLogService.log("复购提醒台", "修改", t.getRemindNo(),
                "登记跟进结果：" + response + (remark == null || remark.isBlank() ? "" : "，" + remark));
        return t;
    }

    /** 预计剩余天数，前端列表直接展示「剩 N 天」 */
    public static long daysLeft(LocalDate end) {
        return ChronoUnit.DAYS.between(LocalDate.now(), end);
    }
}
