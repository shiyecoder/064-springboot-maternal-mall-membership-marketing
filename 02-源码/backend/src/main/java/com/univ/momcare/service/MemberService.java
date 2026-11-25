package com.univ.momcare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.entity.BabyProfile;
import com.univ.momcare.entity.Member;
import com.univ.momcare.mapper.BabyProfileMapper;
import com.univ.momcare.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 会员等级规则（需求文档 5.6）：累计消费金额 + 消费次数双条件，取满足的最高等级。
 * 每晚由定时任务重算；也可由商城运营手动触发，触发后写操作日志留痕。
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final BabyProfileMapper babyMapper;
    private final OperLogService operLogService;

    /** 等级阶梯：金额阈值与次数阈值需同时满足 */
    private static final Object[][] LEVELS = {
            {"黑卡", 8000.0, 20},
            {"金卡", 3000.0, 10},
            {"银卡", 1000.0, 5},
            {"普通会员", 0.0, 0}
    };

    @Transactional
    public Map<String, Object> recalcLevel(Long memberId) {
        Member m = memberMapper.selectById(memberId);
        if (m == null) {
            throw new com.univ.momcare.common.BizException("会员不存在");
        }
        String old = m.getLevelCode();
        double total = m.getTotalAmount() == null ? 0 : m.getTotalAmount().doubleValue();
        int count = m.getOrderCount() == null ? 0 : m.getOrderCount();
        String target = "普通会员";
        for (Object[] lv : LEVELS) {
            if (total >= (Double) lv[1] && count >= (Integer) lv[2]) {
                target = (String) lv[0];
                break;
            }
        }
        m.setLevelCode(target);
        memberMapper.updateById(m);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("memberId", memberId);
        out.put("oldLevel", old);
        out.put("newLevel", target);
        out.put("totalAmount", total);
        out.put("orderCount", count);
        out.put("changed", !target.equals(old));
        operLogService.log("会员档案", "流转", m.getMemberNo(),
                "等级重算：" + old + " → " + target + "（累计 " + total + " 元 / " + count + " 次）");
        return out;
    }

    /** 宝宝有效条数回写到 member.baby_count，供列表展示与上限校验 */
    @Transactional
    public int refreshBabyCount(Long memberId) {
        long n = babyMapper.selectCount(new LambdaQueryWrapper<BabyProfile>()
                .eq(BabyProfile::getMemberId, memberId).eq(BabyProfile::getStatus, 1));
        Member m = memberMapper.selectById(memberId);
        if (m != null) {
            m.setBabyCount((int) n);
            memberMapper.updateById(m);
        }
        return (int) n;
    }
}
