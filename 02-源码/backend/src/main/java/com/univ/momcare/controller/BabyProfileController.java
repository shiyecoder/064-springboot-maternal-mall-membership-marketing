package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.BabyProfile;
import com.univ.momcare.mapper.BabyProfileMapper;
import com.univ.momcare.service.CodeGenService;
import com.univ.momcare.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** 宝宝档案：月龄由出生日期自动计算，单会员最多 5 个有效档案 */
@RestController
@RequestMapping("/api/baby-profile")
@RequiredArgsConstructor
public class BabyProfileController extends BaseCrudController<BabyProfile> {

    private final BabyProfileMapper babyMapper;
    private final CodeGenService codeGen;
    private final MemberService memberService;

    @Override
    protected BaseMapper<BabyProfile> mapper() {
        return babyMapper;
    }

    @Override
    protected Class<BabyProfile> clazz() {
        return BabyProfile.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"baby_no", "baby_name"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"member_id", "gender", "milk_stage", "diaper_size", "status"};
    }

    /**
     * 月龄算法（需求文档 11 节边界用例）：按整月计算，当月最后一天出生不额外多算一个月。
     * 例：1 月 31 日出生，到 2 月 28 日仍为 0 个月，3 月 31 日才是 2 个月。
     */
    public static int monthAge(LocalDate birthday, LocalDate today) {
        if (birthday == null || birthday.isAfter(today)) {
            return 0;
        }
        int months = (today.getYear() - birthday.getYear()) * 12 + today.getMonthValue() - birthday.getMonthValue();
        if (today.getDayOfMonth() < Math.min(birthday.getDayOfMonth(), today.lengthOfMonth())) {
            months--;
        }
        return Math.max(0, Math.min(72, months));
    }

    @Override
    @PostMapping
    public R<Long> add(@RequestBody BabyProfile body) {
        validate(body);
        long exist = babyMapper.selectCount(new LambdaQueryWrapper<BabyProfile>()
                .eq(BabyProfile::getMemberId, body.getMemberId()).eq(BabyProfile::getStatus, 1));
        if (exist >= 5) {
            throw BizException.state("每个会员最多维护 5 个宝宝档案");
        }
        body.setBabyNo(codeGen.babyNo(body.getMemberId(), exist));
        body.setMonthAge(monthAge(body.getBirthday(), LocalDate.now()));
        if (body.getStatus() == null) {
            body.setStatus(1);
        }
        R<Long> r = super.add(body);
        memberService.refreshBabyCount(body.getMemberId());
        return r;
    }

    @Override
    @PutMapping("/{id}")
    public R<Boolean> edit(@PathVariable Long id, @RequestBody BabyProfile body) {
        validate(body);
        // 出生日期被修改导致月龄跨段位时，由前端二次确认；这里只重算月龄，不改人工填写的段位
        body.setMonthAge(monthAge(body.getBirthday(), LocalDate.now()));
        R<Boolean> r = super.edit(id, body);
        memberService.refreshBabyCount(body.getMemberId());
        return r;
    }

    private void validate(BabyProfile b) {
        if (b.getBirthday() == null) {
            throw new BizException("请选择出生日期");
        }
        if (b.getBirthday().isAfter(LocalDate.now())) {
            throw new BizException("出生日期不得晚于今天");
        }
        if (b.getBabyName() == null || b.getBabyName().isBlank()) {
            throw new BizException("请填写宝宝昵称");
        }
        long days = ChronoUnit.DAYS.between(b.getBirthday(), LocalDate.now());
        if (days > 365 * 7) {
            throw new BizException("月龄超过 72 个月，不在母婴适龄范围内");
        }
    }
}
