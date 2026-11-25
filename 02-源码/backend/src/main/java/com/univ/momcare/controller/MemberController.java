package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.common.PageResult;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.ChurnScore;
import com.univ.momcare.entity.Member;
import com.univ.momcare.mapper.ChurnScoreMapper;
import com.univ.momcare.mapper.MemberMapper;
import com.univ.momcare.service.ChurnService;
import com.univ.momcare.service.CodeGenService;
import com.univ.momcare.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** 会员档案：CRUD + 等级重算 + 流失风险详情 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController extends BaseCrudController<Member> {

    private final MemberMapper memberMapper;
    private final ChurnScoreMapper scoreMapper;
    private final MemberService memberService;
    private final ChurnService churnService;
    private final CodeGenService codeGen;

    @Override
    protected BaseMapper<Member> mapper() {
        return memberMapper;
    }

    @Override
    protected Class<Member> clazz() {
        return Member.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"member_no", "nick_name", "mobile"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"level_code", "risk_level", "status", "register_channel"};
    }

    /** 新增：编号由后端生成；手机号唯一由数据库索引兜底 */
    @Override
    @PostMapping
    public R<Long> add(@RequestBody Member body) {
        if (body.getMobile() == null || !body.getMobile().matches("1\\d{10}")) {
            throw new BizException("手机号必须是 11 位数字");
        }
        if (body.getNickName() == null || body.getNickName().length() < 2) {
            throw new BizException("昵称至少 2 个字");
        }
        Long exists = memberMapper.selectCount(new LambdaQueryWrapper<Member>()
                .eq(Member::getMobile, body.getMobile()));
        if (exists != null && exists > 0) {
            throw BizException.dup("该手机号已注册会员");
        }
        body.setMemberNo(codeGen.memberNo());
        if (body.getLevelCode() == null) {
            body.setLevelCode("普通会员");
        }
        if (body.getRiskLevel() == null) {
            body.setRiskLevel("未评估");
        }
        if (body.getStatus() == null) {
            body.setStatus("正常");
        }
        return super.add(body);
    }

    /** 按累计消费与次数重算等级，写留痕 */
    @PostMapping("/{id}/level-recalc")
    public R<Map<String, Object>> recalc(@PathVariable Long id) {
        return R.ok(memberService.recalcLevel(id));
    }

    /** 流失风险：最新快照 + 五因素明细 + 召回建议 */
    @GetMapping("/{id}/churn-score")
    public R<Map<String, Object>> churnScore(@PathVariable Long id) {
        ChurnScore sc = scoreMapper.selectOne(new LambdaQueryWrapper<ChurnScore>()
                .eq(ChurnScore::getMemberId, id).orderByDesc(ChurnScore::getCalcTime).last("limit 1"));
        Map<String, Object> out = new LinkedHashMap<>();
        Member m = memberMapper.selectById(id);
        out.put("member", m);
        out.put("latest", sc);
        out.put("rule", churnService.activeRule());
        out.put("rebuild", churnService.calculate(id));
        return R.ok(out);
    }

    /** 宝宝数量回写 */
    @PostMapping("/{id}/refresh-baby-count")
    public R<Integer> refreshBaby(@PathVariable Long id) {
        return R.ok(memberService.refreshBabyCount(id));
    }
}
