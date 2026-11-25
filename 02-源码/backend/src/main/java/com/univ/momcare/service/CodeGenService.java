package com.univ.momcare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 业务编号生成：前缀 + yyMMdd + 序号。
 * 序号取「当天已有条数 + 1」，同一事务内由数据库唯一索引兜底，重复会抛 4009。
 */
@Service
@RequiredArgsConstructor
public class CodeGenService {

    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");

    private final MemberMapper memberMapper;
    private final BabyProfileMapper babyProfileMapper;
    private final BabyGoodsMapper goodsMapper;
    private final CouponMapper couponMapper;
    private final CouponReceiveMapper couponReceiveMapper;
    private final MallOrderMapper orderMapper;
    private final PointLogMapper pointLogMapper;
    private final RepurchaseTaskMapper repurchaseMapper;

    public String memberNo() {
        return "HY" + LocalDate.now().format(YYMMDD) + seq4(memberMapper.selectCount(null));
    }

    public String babyNo(Long memberId, long exist) {
        return "BB" + String.format("%04d", memberId % 10000) + String.format("%02d", exist + 1);
    }

    public String goodsNo() {
        return "MY" + LocalDate.now().format(YYMMDD) + seq4(goodsMapper.selectCount(null));
    }

    public String couponNo() {
        return "QY" + LocalDate.now().format(YYMMDD) + seq4(couponMapper.selectCount(null));
    }

    public String receiveNo() {
        return "LQ" + LocalDate.now().format(YYMMDD) + seq6(couponReceiveMapper.selectCount(null));
    }

    public String orderNo() {
        return "MO" + LocalDate.now().format(YYMMDD) + seq6(orderMapper.selectCount(null));
    }

    public String pointNo() {
        return "JF" + LocalDate.now().format(YYMMDD) + seq6(pointLogMapper.selectCount(null));
    }

    public String remindNo() {
        return "FG" + LocalDate.now().format(YYMMDD) + seq4(repurchaseMapper.selectCount(null));
    }

    private String seq4(Long n) {
        return String.format("%04d", (n % 9000) + 1);
    }

    private String seq6(Long n) {
        return String.format("%06d", (n % 900000) + 1);
    }
}
