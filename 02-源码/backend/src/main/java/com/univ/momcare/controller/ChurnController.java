package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.common.PageResult;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.ChurnAdjust;
import com.univ.momcare.entity.ChurnRule;
import com.univ.momcare.entity.ChurnScore;
import com.univ.momcare.mapper.ChurnAdjustMapper;
import com.univ.momcare.mapper.ChurnRuleMapper;
import com.univ.momcare.mapper.ChurnScoreMapper;
import com.univ.momcare.service.ChurnService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 创新点接口：规则配置（版本化）、风险计算（留痕）、人工修正（原值保留）、看板聚合。
 */
@RestController
@RequestMapping("/api/churn")
@RequiredArgsConstructor
public class ChurnController {

    private final ChurnService churnService;
    private final ChurnRuleMapper ruleMapper;
    private final ChurnScoreMapper scoreMapper;
    private final ChurnAdjustMapper adjustMapper;

    /** 当前生效规则 + 历史版本 */
    @GetMapping("/rule")
    public R<Map<String, Object>> rule() {
        List<ChurnRule> all = ruleMapper.selectList(new LambdaQueryWrapper<ChurnRule>()
                .orderByDesc(ChurnRule::getId));
        return R.ok(Map.of("active", churnService.activeRule(), "versions", all));
    }

    /** 保存并发布新规则版本：权重合计必须 100，旧版本置为历史 */
    @PostMapping("/rule")
    public R<ChurnRule> saveRule(@RequestBody ChurnRule body) {
        int sum = body.getWeightGap() + body.getWeightFreq() + body.getWeightAmount()
                + body.getWeightCoupon() + body.getWeightAge();
        if (sum != 100) {
            throw new BizException("五项权重合计必须等于 100，当前为 " + sum);
        }
        if (body.getRemark() == null || body.getRemark().trim().length() < 5) {
            throw new BizException("变更说明不少于 5 个字");
        }
        List<ChurnRule> olds = ruleMapper.selectList(new LambdaQueryWrapper<ChurnRule>()
                .eq(ChurnRule::getStatus, "生效"));
        for (ChurnRule r : olds) {
            r.setStatus("历史");
            ruleMapper.updateById(r);
        }
        int max = ruleMapper.selectList(null).stream()
                .mapToInt(r -> Integer.parseInt(r.getRuleVersion().replaceAll("\\D", "")))
                .max().orElse(0);
        body.setId(null);
        body.setRuleVersion("v" + (max + 1));
        body.setStatus("生效");
        body.setEffectTime(LocalDateTime.now());
        ruleMapper.insert(body);
        return R.ok(body);
    }

    @GetMapping("/board")
    public R<Map<String, Object>> board() {
        return R.ok(churnService.board());
    }

    /**
     * 风险分页：每位会员只返回**最新一次**计算快照。
     * 重算会不断新增快照（历史保留），若直接按表分页，同一会员会重复出现。
     */
    @GetMapping("/page")
    public R<PageResult<ChurnScore>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String riskLevel,
                                          @RequestParam(required = false) String recallStatus,
                                          @RequestParam(required = false) String keyword) {
        List<ChurnScore> all = scoreMapper.selectList(new LambdaQueryWrapper<ChurnScore>()
                .orderByDesc(ChurnScore::getCalcTime).orderByDesc(ChurnScore::getId));
        // 按会员去重，保留最新快照
        Map<Long, ChurnScore> latest = new LinkedHashMap<>();
        for (ChurnScore sc : all) {
            latest.putIfAbsent(sc.getMemberId(), sc);
        }
        List<ChurnScore> rows = new ArrayList<>(latest.values());
        if (riskLevel != null && !riskLevel.isBlank()) {
            rows.removeIf(sc -> !riskLevel.equals(sc.getRiskLevel()));
        }
        if (recallStatus != null && !recallStatus.isBlank()) {
            rows.removeIf(sc -> !recallStatus.equals(sc.getRecallStatus()));
        }
        rows.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));

        long total = rows.size();
        long s = Math.min(Math.max(size, 1), 100);
        int from = (int) Math.min((Math.max(page, 1) - 1) * s, total);
        int to = (int) Math.min(from + s, total);
        List<ChurnScore> slice = new ArrayList<>(rows.subList(from, to));
        nameFill.fill(slice);

        PageResult<ChurnScore> pr = new PageResult<>();
        pr.setRecords(slice);
        pr.setTotal(total);
        pr.setPage(Math.max(page, 1));
        pr.setSize(s);
        pr.setPages(s == 0 ? 0 : (total + s - 1) / s);
        return R.ok(pr);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.univ.momcare.common.NameFill nameFill;

    /** 单个会员重算 */
    @PostMapping("/calculate/{memberId}")
    public R<ChurnScore> calculate(@PathVariable Long memberId) {
        return R.ok(churnService.calculate(memberId));
    }

    /** 全量重算：只影响新快照，历史保留 */
    @PostMapping("/calculate-all")
    public R<Integer> calculateAll() {
        return R.ok(churnService.calculateAll());
    }

    /** 人工修正：只能一次，原因不少于 5 字，原值保留 */
    @PostMapping("/adjust/{scoreId}")
    public R<Void> adjust(@PathVariable Long scoreId, @RequestBody Map<String, String> body) {
        churnService.adjust(scoreId, body.get("newLevel"), body.get("reason"));
        return R.ok();
    }

    /** 分配给导购 */
    @PostMapping("/assign/{scoreId}")
    public R<Void> assign(@PathVariable Long scoreId, @RequestParam Long ownerId) {
        churnService.assign(scoreId, ownerId);
        return R.ok();
    }

    /** 修正记录 */
    @GetMapping("/adjusts")
    public R<List<ChurnAdjust>> adjusts() {
        return R.ok(adjustMapper.selectList(new LambdaQueryWrapper<ChurnAdjust>()
                .orderByDesc(ChurnAdjust::getAdjustTime)));
    }
}
