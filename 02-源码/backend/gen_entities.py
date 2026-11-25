#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""按 sql/init.sql 建出的真实表结构生成实体与 Mapper，避免手写字段类型出错。
运行前需先导入 init.sql 到 new_064。
"""
import os
import subprocess

BASE = os.path.dirname(os.path.abspath(__file__))
JAVA = os.path.join(BASE, "src/main/java/com/univ/momcare")

TABLES = {
    "member": ("Member", {"memberId": "memberName"}),
    "baby_profile": ("BabyProfile", {"memberId": "memberName"}),
    "baby_goods": ("BabyGoods", {}),
    "coupon": ("Coupon", {}),
    "coupon_receive": ("CouponReceive", {"memberId": "memberName", "couponId": "couponName"}),
    "mall_order": ("MallOrder", {"memberId": "memberName"}),
    "mall_order_item": ("MallOrderItem", {}),
    "point_log": ("PointLog", {"memberId": "memberName", "operatorId": "operatorName"}),
    "repurchase_task": ("RepurchaseTask", {"memberId": "memberName", "goodsId": "goodsName", "ownerId": "ownerName"}),
    "goods_review": ("GoodsReview", {"memberId": "memberName", "goodsId": "goodsName"}),
    "churn_rule": ("ChurnRule", {}),
    "churn_score": ("ChurnScore", {"memberId": "memberName", "ownerId": "ownerName"}),
    "churn_adjust": ("ChurnAdjust", {"memberId": "memberName"}),
}

TYPE_MAP = {
    "bigint": "Long", "int": "Integer", "tinyint": "Integer", "smallint": "Integer",
    "decimal": "BigDecimal", "varchar": "String", "char": "String", "text": "String",
    "datetime": "LocalDateTime", "date": "LocalDate", "time": "LocalTime", "double": "Double", "float": "Float",
}

Camel = lambda s: "".join(p[:1].upper() + p[1:] for p in s.split("_"))


def query(sql):
    out = subprocess.check_output(
        ["/opt/homebrew/bin/mysql", "-uroot", "-p123456", "-N", "-B", "-e", sql],
        stderr=subprocess.DEVNULL)
    return [line.split("\t") for line in out.decode("utf-8").splitlines() if line.strip()]


for table, (cls, names) in TABLES.items():
    cols = query("select column_name, data_type, column_comment, is_nullable, column_key "
                 "from information_schema.columns where table_schema='new_064' and table_name='%s' "
                 "order by ordinal_position" % table)
    imports = {"java.io.Serializable", "com.baomidou.mybatisplus.annotation.*", "lombok.Data"}
    body = []
    need_bigdecimal = any(c[1] == "decimal" for c in cols)
    need_dt = any(c[1] == "datetime" for c in cols)
    need_date = any(c[1] == "date" for c in cols)
    need_time = any(c[1] == "time" for c in cols)

    for name, dtype, comment, nullable, key in cols:
        jt = TYPE_MAP.get(dtype, "String")
        f = Camel(name)
        f = f[0].lower() + f[1:]
        ann = []
        if key == "PRI":
            ann.append("    @TableId(type = IdType.AUTO)")
        if name == "deleted":
            ann.append("    @TableLogic")
        body.append("\n")
        body.append("    /** %s */\n" % (comment if comment != "" else name))
        for a in ann:
            body.append(a + "\n")
        body.append("    private %s %s;\n" % (jt, f))

    # 瞬态展示字段：外键 ID → 名称，供 NameFill 回填
    for fk, nm in names.items():
        body.append("\n")
        body.append("    /** 关联名称，列表展示用，不落库 */\n")
        body.append("    @TableField(exist = false)\n")
        body.append("    private String %s;\n" % nm)
    if table == "mall_order":
        body.append("\n    /** 订单明细，详情接口返回，不落库 */\n")
        body.append("    @TableField(exist = false)\n")
        body.append("    private java.util.List<MallOrderItem> items;\n")

    imp = ["import java.io.Serializable;",
           "import com.baomidou.mybatisplus.annotation.*;",
           "import lombok.Data;"]
    if need_bigdecimal:
        imp.append("import java.math.BigDecimal;")
    if need_dt:
        imp.append("import java.time.LocalDateTime;")
    if need_date:
        imp.append("import java.time.LocalDate;")
    if need_time:
        imp.append("import java.time.LocalTime;")

    src = """package com.univ.momcare.entity;

%s

/**
 * %s（表 `%s`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("%s")
public class %s implements Serializable {
%s

    private static final long serialVersionUID = 1L;
}
""" % ("\n".join(sorted(imp)), table, table, table, cls, "".join(body))
    with open(os.path.join(JAVA, "entity", cls + ".java"), "w", encoding="utf-8") as f:
        f.write(src)

    mapper = """package com.univ.momcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.%s;
import org.apache.ibatis.annotations.Mapper;

/** %s Mapper */
@Mapper
public interface %sMapper extends BaseMapper<%s> {
}
""" % (cls, table, cls, cls)
    with open(os.path.join(JAVA, "mapper", cls + "Mapper.java"), "w", encoding="utf-8") as f:
        f.write(mapper)
    print("generated", cls)
