#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 sql/init.sql：建库、24 张表、字典/菜单/角色/账号、六个月跨度测试数据。
数据之间保持一致：会员累计消费/积分由已完成订单推导，券领取量由领券记录统计，
复购提醒来自最近一次已完成订单，流失分来自会员消费行为。
运行：python3 sql/gen_sql.py
"""
import os
import random
from datetime import datetime, timedelta

BASE = os.path.dirname(os.path.abspath(__file__))
random.seed(20261102)

NOW = datetime(2026, 11, 2, 8, 20, 0)
PWD = "$2b$10$4M7vvaPh1pirsbVTyYtSW.8kMbSN2InISXsBXhmjGkXCx3gUySSfO"  # 123456

def d(dt):
    return dt.strftime("%Y-%m-%d")

def dt_s(dt):
    return dt.strftime("%Y-%m-%d %H:%M:%S")

def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"

def rows(table, cols, data, chunk=40):
    out = []
    for i in range(0, len(data), chunk):
        part = data[i:i + chunk]
        vals = ",\n".join("(" + ", ".join(q(x) for x in r) + ")" for r in part)
        out.append("INSERT INTO `%s` (%s) VALUES\n%s;" % (
            table, ", ".join("`%s`" % c for c in cols), vals))
    return "\n".join(out)

COMMON = ["create_by", "create_time", "update_by", "update_time", "deleted", "version"]

# ---------------------------------------------------------------- 数据准备
SURNAME = "王李陈张刘赵孙周吴郑何黄林徐马朱胡郭高罗"
GIVEN = ["雅琴", "思远", "梦琪", "若彤", "一诺", "悦然", "子墨", "欣妍", "junjun", "欣然",
         "语桐", "浩然", "晟睿", "芷若", "书瑶", "念安", "知夏", "亦辰", "星辰", "安然"]
BABY_NAMES = ["安宝", "小满", "团团", "糯米", "果果", "糖糖", "豆豆", "一一", "沐沐", "可可",
              "乐乐", "米粒", "小鹿", "年年", "多多", "叮当"]
CHANNELS = ["门店扫码", "小程序", "导购推荐"]
LEVELS = ["普通会员", "银卡", "金卡", "黑卡"]
BRANDS = {"奶粉": ["爱他美", "惠氏", "美赞臣", "雅培", "a2", "飞鹤"],
          "纸尿裤": ["帮宝适", "花王", "尤妮佳", "好奇", "大王"],
          "辅食": ["嘉宝", "亨氏", "小皮", "英氏"],
          "洗护": ["贝亲", "全棉时代", "郁美净", "艾维诺"],
          "玩具": ["费雪", "澳贝", "曼哈顿"],
          "孕妇用品": ["美德乐", "十月结晶", "子初"]}
# 商品主图编号（uploads/seed/goods/NN.jpg 实测内容：奶粉/纸尿裤/辅食/洗护/玩具/孕妇用品各若干），
# 按品类就近匹配且保证相邻两行不同图。
GOODS_IMG = [13, 14, 13, 14, 1, 2, 3, 4, 16, 15, 12, 7,
             5, 11, 6, 9, 8, 10, 14, 2, 16, 11, 6, 11]
GOODS_TPL = [
    ("奶粉", "爱他美卓萃 {s} 段 800g", "800g/罐", 328, 298, 0, 6),
    ("奶粉", "惠氏启赋蓝钻 {s} 段 900g", "900g/罐", 368, 338, 6, 12),
    ("奶粉", "美赞臣蓝臻 {s} 段 800g", "800g/罐", 398, 368, 12, 36),
    ("奶粉", "飞鹤星飞帆 {s} 段 700g", "700g/罐", 268, 248, 12, 36),
    ("纸尿裤", "帮宝适一级帮 {z} 码 42 片", "{z}码42片", 168, 152, 9, 24),
    ("纸尿裤", "花王妙而舒 {z} 码 54 片", "{z}码54片", 138, 128, 4, 12),
    ("纸尿裤", "尤妮佳 moony {z} 码 48 片", "{z}码48片", 128, 118, 0, 6),
    ("纸尿裤", "好奇金装 {z} 码 50 片", "{z}码50片", 118, 108, 12, 36),
    ("辅食", "嘉宝原味米粉 250g", "250g/盒", 58, 52, 6, 18),
    ("辅食", "亨氏果泥 苹果味 120g", "120g/袋", 19.9, 17.9, 6, 24),
    ("辅食", "小皮有机菠菜面 200g", "200g/盒", 46, 42, 12, 36),
    ("洗护", "贝亲宽口径奶瓶 240ml", "240ml", 96, 88, 0, 24),
    ("洗护", "全棉时代婴儿棉柔巾 100 抽", "100抽/包", 39.9, 35.9, 0, 36),
    ("洗护", "艾维诺婴儿润肤露 227g", "227g/瓶", 118, 108, 0, 72),
    ("玩具", "费雪声光安抚海马", "单只", 129, 118, 0, 36),
    ("玩具", "澳贝摇铃套装 5 件", "5件/套", 89, 79, 3, 18),
    ("孕妇用品", "美德乐丝韵吸奶器单边", "单边电动", 1299, 1199, 0, 0),
    ("孕妇用品", "十月结晶待产包 32 件", "32件/套", 268, 248, 0, 0),
    ("奶粉", "雅培菁挚有机 {s} 段 800g", "800g/罐", 358, 328, 12, 36),
    ("纸尿裤", "大王天使 {z} 码 44 片", "{z}码44片", 148, 138, 9, 24),
    ("辅食", "英氏磨牙棒 60g", "60g/盒", 26.9, 23.9, 6, 18),
    ("洗护", "郁美净儿童霜 50g", "50g/瓶", 29.9, 26.9, 0, 72),
    ("玩具", "曼哈顿牙胶球", "单只", 79, 69, 3, 12),
    ("洗护", "子初孕妇洗发水 300ml", "300ml", 69, 62, 0, 0),
]

goods = []
for i, (typ, tpl, spec, sp, mp, a0, a1) in enumerate(GOODS_TPL, 1):
    stage = random.choice(["1", "2", "3", "4"]) if "{s}" in tpl else ""
    size = random.choice(["S", "M", "L", "XL", "XXL"]) if "{z}" in tpl else ""
    name = tpl.replace("{s}", stage).replace("{z}", size)
    spec = spec.replace("{z}", size)
    stock = random.choice([0, 5, 8, 12, 23, 46, 58, 72, 96, 120])
    if i <= 18:
        stock = max(stock, 6)
    status = "在售" if stock > 0 else "已售罄"
    if i in (20, 24):
        status = "已下架"
    goods.append(dict(id=i, no="MY2610%04d" % (1000 + i), name=name, brand=BRANDS[typ][i % len(BRANDS[typ])],
                      type=typ, a0=a0, a1=a1, spec=spec, sp=sp, mp=mp, stock=stock, status=status,
                      img="/uploads/seed/goods/%02d.jpg" % GOODS_IMG[i - 1]))

# 会员：32 条，注册时间分散在近 6 个月
members = []
for i in range(1, 33):
    created = NOW - timedelta(days=random.randint(4, 178), hours=random.randint(0, 12))
    m = dict(id=i, no="HY26%04d%04d" % (created.month * 100 + created.day, 1000 + i),
             mobile="138%08d" % (10000000 + i * 137),
             # 姓名用二维错位取，避免 i 与 i+20 撞成同一个名字（如 6/26 都叫孙子墨）
             nick=SURNAME[i % len(SURNAME)] + GIVEN[((i // len(SURNAME)) * 3 + i) % len(GIVEN)],
             channel=CHANNELS[i % 3], created=created, total=0.0, cnt=0, point=0,
             last_buy=None, risk="未评估", status="正常")
    if i in (6, 23):
        m["status"] = "冻结"
    members.append(m)

# 宝宝档案：48 条，会员最多 3 个
babies = []
bid = 1
for m in members:
    n = 2 if m["id"] % 4 else 1
    for k in range(n):
        months = random.choice([4, 7, 9, 12, 15, 18, 20, 24, 28, 33, 39, 48, 56])
        birthday = (NOW - timedelta(days=months * 30 + random.randint(0, 20))).date()
        stage = "1段" if months < 6 else "2段" if months < 12 else "3段" if months < 36 else "4段"
        size = "S" if months < 6 else "M" if months < 12 else "L" if months < 20 else "XL" if months < 30 else "XXL"
        babies.append(dict(id=bid, no="BB%04d%02d" % (m["id"], k + 1), mid=m["id"],
                           name=BABY_NAMES[(bid - 1) % len(BABY_NAMES)], gender=["女", "男", "未知"][bid % 3],
                           birthday=birthday, month=months,
                           h=round(58 + months * 0.9 + random.uniform(-2, 2), 1),
                           w=round(5.2 + months * 0.28 + random.uniform(-.4, .5), 1),
                           allergy=random.choice([None, None, None, "牛奶蛋白过敏", "湿疹", "花生过敏"]),
                           stage=stage, size=size, status=1 if bid % 11 else 0,
                           created=m["created"] + timedelta(days=random.randint(1, 6))))
        bid += 1
for i, m in enumerate(members, 1):
    if i in (7, 19):
        m["created"] = NOW - timedelta(hours=i, minutes=17)
for m in members:
    m["baby_count"] = sum(1 for b in babies if b["mid"] == m["id"] and b["status"] == 1)

# 门户演示账号固定：sys_user 12 对 member 12，sys_user 13 对 member 13。
# 门户接口以当前登录用户 ID 作为会员 ID 取数，两边 ID 必须一一对齐。
DEMO_MEMBERS = {
    12: dict(nick="王雅琴", mobile="13800002210", total=3860.00, cnt=16, point=3860, level="金卡", risk="低风险"),
    13: dict(nick="李沐宸", mobile="13800002211", total=9240.00, cnt=28, point=9240, level="黑卡", risk="低风险"),
}
for mid, p in DEMO_MEMBERS.items():
    m = members[mid - 1]
    m.update(nick=p["nick"], mobile=p["mobile"], total=p["total"], cnt=p["cnt"], point=p["point"],
             risk=p["risk"], last_buy=NOW - timedelta(days=2), level=p["level"])

# 门户演示宝宝：会员 12 的宝宝 21「安宝」20 个月 3 段 L 码，会员 13 的宝宝 22「糖糖」24 个月
DEMO_BABIES = {
    21: dict(name="安宝", month=20, stage="3段", size="L"),
    22: dict(name="糖糖", month=24, stage="3段", size="XL"),
}
for b in babies:
    p = DEMO_BABIES.get(b["id"])
    if not p:
        continue
    b.update(name=p["name"], month=p["month"], stage=p["stage"], size=p["size"], status=1,
             birthday=(NOW - timedelta(days=p["month"] * 30 + 6)).date(),
             h=round(58 + p["month"] * 0.9, 1), w=round(5.2 + p["month"] * 0.28, 1))
for m in members:
    m["baby_count"] = sum(1 for b in babies if b["mid"] == m["id"] and b["status"] == 1)

# 优惠券 12 张
coupon_def = [
    ("金卡专享满 300 减 50", "满减", 300, 50, "全场通用", "定向发放", -1, 60, 2000),
    ("新人礼包满 99 减 20", "满减", 99, 20, "全场通用", "新人礼包", -32, 60, 5000),
    ("纸尿裤品类满 199 减 30", "满减", 199, 30, "指定品类", "主动领取", -18, 14, 800),
    ("黑卡 9 折券", "折扣", 0, 9, "全场通用", "定向发放", -62, 28, 300),
    ("辅食立减 10 元", "立减", 0, 10, "指定品类", "积分兑换", -93, -3, 1000),
    ("奶粉满 600 减 80", "满减", 600, 80, "指定品类", "定向发放", -6, 54, 600),
    ("全场满 199 减 15", "满减", 199, 15, "全场通用", "主动领取", -12, 30, 3000),
    ("洗护 8.8 折", "折扣", 0, 8.8, "指定品类", "新人礼包", -45, 45, 1500),
    ("满 99 免邮券", "免邮", 99, 8, "全场通用", "主动领取", -25, 35, 5000),
    ("玩具满 129 减 20", "满减", 129, 20, "指定品类", "积分兑换", -70, 10, 800),
    ("孕妇用品满 500 减 60", "满减", 500, 60, "指定品类", "定向发放", -20, 40, 400),
    ("周末限定立减 5 元", "立减", 0, 5, "全场通用", "主动领取", -50, -1, 2000),
]
coupons = []
for i, (nm, typ, mn, da, lt, gt, s_off, e_off, total) in enumerate(coupon_def, 1):
    vs = NOW + timedelta(days=s_off)
    ve = NOW + timedelta(days=e_off)
    status = "发放中"
    if e_off < 0:
        status = "已过期"
    elif i in (3,):
        status = "已停发"
    elif i in (6,):
        status = "待发布"
    coupons.append(dict(id=i, no="QY2610%04d" % (1000 + i), name=nm, type=typ, min=mn, disc=da,
                        limit=lt, grant=gt, vs=vs, ve=ve, total=total, recv=0, status=status,
                        created=NOW + timedelta(days=min(s_off, -1))))

# 领券记录：120 条
receives = []
rid = 1
for c in coupons:
    for m in members:
        if rid > 120:
            break
        if random.random() > 0.35:
            continue
        if m["status"] != "正常":
            continue
        t = c["created"] + timedelta(days=random.randint(0, max(1, (NOW - c["created"]).days)))
        if t > NOW:
            t = NOW - timedelta(days=1)
        receives.append(dict(id=rid, no="LQ2610%06d" % (100000 + rid), cid=c["id"], mid=m["id"],
                             channel=random.choice(["商城领取", "导购发放", "新人礼包", "积分兑换"]),
                             time=t, use="未使用", oid=None, utime=None,
                             grant=random.choice([None, 2, 3, 4])))
        c["recv"] += 1
        rid += 1

# 订单 76 条（覆盖 7 种状态）
orders = []
items = []
oid = 1
iid = 1
status_plan = (["已完成"] * 32 + ["待发货"] * 11 + ["已发货"] * 14 + ["待支付"] * 6 +
               ["已取消"] * 6 + ["退货中"] * 3 + ["已退款"] * 4)
random.shuffle(status_plan)
for st in status_plan:
    m = random.choice([x for x in members if x["status"] == "正常"])
    if random.random() < 0.16:
        created = NOW - timedelta(hours=random.randint(1, 9), minutes=random.randint(0, 59))
    else:
        created = NOW - timedelta(days=random.randint(1, 170), hours=random.randint(0, 20),
                                  minutes=random.randint(0, 59))
    n = random.randint(1, 3)
    chosen = random.sample(goods, n)
    g_amount = 0.0
    for g in chosen:
        qty = random.randint(1, 2)
        price = g["mp"] if g["mp"] else g["sp"]
        g_amount += float(price) * qty
        items.append(dict(id=iid, oid=oid, gid=g["id"], name=g["name"], spec=g["spec"],
                          price=price, qty=qty, amount=round(float(price) * qty, 2)))
        iid += 1
    # 优惠：从该会员已有券里挑一张满足门槛且未使用的
    cand = [r for r in receives if r["mid"] == m["id"] and r["use"] == "未使用"]
    disc = 0.0
    rid_used = None
    if cand and random.random() < 0.55:
        r0 = random.choice(cand)
        c0 = coupons[r0["cid"] - 1]
        if c0["type"] == "满减" and g_amount >= float(c0["min"]):
            disc = float(c0["disc"]); rid_used = r0["id"]
        elif c0["type"] == "立减":
            disc = float(c0["disc"]); rid_used = r0["id"]
        elif c0["type"] == "折扣" and st != "已取消":
            disc = round(g_amount * (1 - float(c0["disc"]) / 10), 2); rid_used = r0["id"]
        if disc > g_amount:
            disc = g_amount
    freight = 0.0 if g_amount - disc >= 199 else 8.0
    pay = round(max(0.0, g_amount - disc + freight), 2)
    gain = int(pay)
    orders.append(dict(id=oid, no="MO26%04d%06d" % (created.month * 100 + created.day, 100000 + oid),
                       mid=m["id"], cnt=sum(1 for _ in chosen), g_amount=round(g_amount, 2),
                       rid=rid_used, disc=round(disc, 2), freight=freight, pay=pay, gain=gain,
                       pay_type=random.choice(["微信", "支付宝", "储值卡"]), status=st,
                       created=created))
    if rid_used and st != "已取消":
        for r in receives:
            if r["id"] == rid_used:
                r["use"] = "已使用"
                r["oid"] = oid
                r["utime"] = created + timedelta(minutes=random.randint(1, 20))
    if st in ("已完成",):
        m["total"] += pay
        m["cnt"] += 1
        m["last_buy"] = max(m["last_buy"], created) if m["last_buy"] else created
        m["point"] += gain
    oid += 1

# 会员历史消费补全：只靠 76 笔订单无法形成真实等级分布，这里按目标占比给每位会员
# 回填累计消费与消费次数，使等级分布接近门店实际（黑卡 12% / 金卡 26% / 银卡 34% / 普通 28%）。
# 12、13 为门户演示账号，保持上面写死的等级与金额不动。
TIER_PLAN = {
    "黑卡": [(3, 12860, 34), (21, 9420, 27), (27, 8210, 23), (30, 15680, 41)],
    "金卡": [(2, 6420, 18), (5, 4380, 14), (9, 7120, 21), (14, 3260, 11),
             (18, 5140, 15), (24, 3980, 12), (28, 6840, 19), (31, 3620, 11)],
    "银卡": [(1, 2680, 9), (4, 1420, 6), (6, 1980, 7), (8, 2340, 8), (11, 1860, 6),
             (15, 2760, 9), (17, 1240, 5), (20, 2120, 7), (22, 1580, 6), (25, 2460, 8), (29, 1340, 5)]
}
TIER_RECENT = {"黑卡": (2, 18), "金卡": (4, 30), "银卡": (10, 60)}
for tier, rows_ in TIER_PLAN.items():
    for mid, amount, cnt in rows_:
        m = members[mid - 1]
        if m["status"] == "冻结":
            continue
        lo, hi = TIER_RECENT[tier]
        m["total"] = float(amount)
        m["cnt"] = cnt
        m["last_buy"] = NOW - timedelta(days=random.randint(lo, hi), hours=random.randint(0, 12))
for m in members:
    if m["id"] in (12, 13):
        continue
    if all(m["id"] != mid for rows_ in TIER_PLAN.values() for mid, _, _ in rows_):
        # 普通会员：金额与次数都低，部分长期未购，用于制造待召回名单
        m["total"] = round(random.uniform(80, 980), 2)
        m["cnt"] = random.randint(1, 4)
        m["last_buy"] = None if m["id"] % 6 == 0 else NOW - timedelta(days=random.randint(35, 150))

# 过期券：有效期结束时间已过的未使用券
for r in receives:
    c = coupons[r["cid"] - 1]
    if r["use"] == "未使用" and c["ve"] < NOW:
        r["use"] = "已过期"

# 积分流水：与会员积分余额对齐
point_logs = []
pid = 1
for m in members:
    bal = 0
    mine = [o for o in orders if o["mid"] == m["id"] and o["status"] == "已完成"]
    mine.sort(key=lambda x: x["created"])
    for o in mine:
        before = bal
        bal += o["gain"]
        point_logs.append(dict(id=pid, no="JF26%06d" % (100000 + pid), mid=m["id"], type="下单获得",
                               chg=o["gain"], before=before, after=bal, biz=o["no"],
                               remark=None, op=None, created=o["created"] + timedelta(minutes=25)))
        pid += 1
    # 积分商城兑换：余额较高的会员会分批兑换，保证「沉淀比」接近门店真实水平（约 1.6-2.0）
    while bal > 600 and m["id"] % 4 != 3:
        use = random.choice([300, 500, 800, 1000])
        use = min(use, bal - 200)
        if use <= 0:
            break
        point_logs.append(dict(id=pid, no="JF26%06d" % (100000 + pid), mid=m["id"], type="兑换扣减",
                               chg=-use, before=bal, after=bal - use, biz="DH26%06d" % (500000 + pid),
                               remark="积分商城兑换纸尿裤试用装", op=m["id"],
                               created=NOW - timedelta(days=random.randint(2, 80))))
        bal -= use
        pid += 1
    if m["id"] % 9 == 0:
        add = random.choice([50, 100])
        point_logs.append(dict(id=pid, no="JF26%06d" % (100000 + pid), mid=m["id"], type="人工调整",
                               chg=add, before=bal, after=bal + add, biz=None,
                               remark="店庆活动补发积分，已与店长确认", op=1,
                               created=NOW - timedelta(days=random.randint(5, 60))))
        bal += add
        pid += 1
    m["point"] = bal

# 商品评价：36 条
reviews = []
revid = 1
for o in [x for x in orders if x["status"] == "已完成"][:18]:
    oi = [it for it in items if it["oid"] == o["id"]][0]
    reviews.append(dict(id=revid, oid=o["id"], gid=oi["gid"], mid=o["mid"],
                        score=random.choice([5, 5, 4, 4, 3, 2]),
                        content=random.choice([
                            "宝宝喝了两罐，便便正常，就是价格有点小贵。",
                            "夜用不漏，但腰围偏紧，建议买大一码。",
                            "包装破损，客服已换货，米粉本身还行。",
                            "一直回购的款，门店自提很快。",
                            "味道宝宝接受，冲调不太容易结块。",
                            "快递很慢，东西没问题。"]),
                        images=None, status=random.choice(["已通过", "已通过", "待审核", "待审核", "已驳回"]),
                        created=o["created"] + timedelta(days=random.randint(1, 6), hours=3)))
    revid += 1

# 复购提醒：28 条，来自最近完成的奶粉/纸尿裤订单
repurchase = []
fpid = 1
seen = set()
for o in sorted([x for x in orders if x["status"] in ("已完成", "已发货", "待发货")], key=lambda x: x["created"], reverse=True):
    if fpid > 28:
        break
    m = members[o["mid"] - 1]
    for it in [x for x in items if x["oid"] == o["id"]]:
        g = goods[it["gid"] - 1]
        if g["type"] not in ("奶粉", "纸尿裤"):
            continue
        if (o["mid"], g["id"]) in seen:
            continue
        seen.add((o["mid"], g["id"]))
        days = 31 if g["type"] == "奶粉" else 24
        end = o["created"].date() + timedelta(days=days)
        plan = datetime.combine(end - timedelta(days=3), datetime.min.time()) + timedelta(hours=10)
        if plan < NOW - timedelta(days=45):
            continue
        repurchase.append(dict(id=fpid, no="FG26%04d" % (1000 + fpid), mid=o["mid"], gid=g["id"],
                               last=d(o["created"]), end=d(end),
                               channel=random.choice(["站内消息", "短信模拟", "导购电话"]),
                               plan=plan,
                               send=random.choice(["待发送", "待发送", "已发送", "已发送", "已跳过"]),
                               resp=random.choice(["无响应", "已复购", "咨询未购", "无响应"]),
                               owner=random.choice([9, 10, 11]), created=NOW - timedelta(days=random.randint(0, 9))))
        fpid += 1
        break

# 流失规则 3 个版本
rules = [
    dict(id=1, name="初始规则", ver="v1", w=[30, 30, 20, 10, 10], hi=80, mid=60, status="历史",
         eff=NOW - timedelta(days=150), remark="上线初版，按经验设定权重"),
    dict(id=2, name="调优规则", ver="v2", w=[35, 25, 20, 10, 10], hi=78, mid=55, status="历史",
         eff=NOW - timedelta(days=80), remark="提高购买间隔权重，降低频次权重"),
    dict(id=3, name="现行规则", ver="v3", w=[35, 25, 20, 10, 10], hi=75, mid=50, status="生效",
         eff=NOW - timedelta(days=13), remark="阈值下调，覆盖更多待召回会员"),
]

# 流失计算快照：40 条
scores = []
scid = 1
for m in members:
    gap_days = (NOW - m["last_buy"]).days if m["last_buy"] else 999
    s_gap = 100 if gap_days >= 90 else int(gap_days / 90 * 100)
    freq = (m["cnt"] / 6.0) if m["created"] < NOW - timedelta(days=90) else m["cnt"]
    s_freq = max(0, min(100, int(100 - freq * 18)))
    avg = m["total"] / m["cnt"] if m["cnt"] else 0
    s_avg = 30 if m["cnt"] == 0 else (25 if m["cnt"] >= 8 else 45 if m["cnt"] >= 4 else 70)
    rv = [r for r in receives if r["mid"] == m["id"]]
    used = sum(1 for r in rv if r["use"] == "已使用")
    s_coupon = 0 if not rv else int(100 - used / len(rv) * 100)
    has_baby = any(b["mid"] == m["id"] for b in babies)
    s_age = 60 if not has_baby else random.choice([30, 40, 55, 65])
    w = rules[2]["w"]
    total = round((s_gap * w[0] + s_freq * w[1] + s_avg * w[2] + s_coupon * w[3] + s_age * w[4]) / 100, 1)
    lvl = "高风险" if total >= 75 else "中风险" if total >= 50 else "低风险"
    if m["cnt"] == 0 and gap_days > 120:
        lvl, total = "已流失", max(total, 92)
    m["risk"] = lvl
    scores.append(dict(id=scid, mid=m["id"], rule=3, ver="v3",
                       inp='{"gapDays": %d, "freq90": %.2f, "avgAmount": %.2f, "couponUseRate": %.2f, "babyMonthAge": %s}' % (
                           gap_days, freq, avg, (used / len(rv) if rv else 0),
                           next((b["month"] for b in babies if b["mid"] == m["id"]), "null")),
                       s=[s_gap, s_freq, s_avg, s_coupon, s_age], total=total, lvl=lvl,
                       concl=("距上次购买 %d 天，消费频次偏低，建议 48 小时内电话召回并发满减券" % gap_days)
                       if lvl in ("高风险", "已流失") else
                       ("消费稳定，按月推送复购提醒即可" if lvl == "低风险" else
                        "客单价下滑或券未使用，建议 7 天内推送复购提醒"),
                       unmet="缺少宝宝档案，月龄因素按基准 60 分计入" if not has_baby else None,
                       suggest="推荐发放「满 199 减 30」定向券" if lvl != "低风险" else "不额外发券，控制营销成本",
                       calc=NOW - timedelta(hours=6, minutes=random.randint(0, 50)),
                       owner=random.choice([5, 6, 7]) if lvl in ("高风险", "已流失") else None,
                       recall=random.choice(["待召回", "跟进中", "已召回"]) if lvl in ("高风险", "已流失") else "无需召回"))
    scid += 1

# 人工修正 4 条
adjusts = [
    (1, 4, "高风险", "中风险", "会员刚在门店消费，系统未及时同步"),
    (2, 7, "已流失", "中风险", "会员为二胎妈妈，哺乳期暂停购买属正常"),
    (3, 4, "高风险", "高风险", "已电话确认预产期在即，维持高风险"),
    (4, 12, "中风险", "低风险", "会员为团购客户，低频但客单高"),
]

# ---------------------------------------------------------------- DDL
DDL = []
DDL.append("""-- ============================================================
-- 芽芽母婴商城与会员营销系统  数据库初始化脚本（MySQL 8.0 / utf8mb4）
-- 库名 new_064；可重复执行：先 DROP 再建，导入后即为完整演示数据
-- 生成脚本：sql/gen_sql.py（改数据请改脚本后重新生成）
-- ============================================================
DROP DATABASE IF EXISTS `new_064`;
CREATE DATABASE `new_064` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `new_064`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;""")

DDL.append("""
-- 1. member 会员
CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_no` varchar(32) NOT NULL COMMENT '规则 HY+yyMMdd+4位序号，全局唯一',
  `mobile` varchar(20) NOT NULL COMMENT '11位，全局唯一，同时作为登录账号',
  `nick_name` varchar(40) NOT NULL COMMENT '2至20字，敏感词过滤',
  `level_code` varchar(20) NOT NULL DEFAULT '普通会员' COMMENT '普通会员/银卡/金卡/黑卡',
  `register_channel` varchar(20) NOT NULL COMMENT '门店扫码/小程序/导购推荐',
  `total_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '已完成订单实付累计，退款完成后回减',
  `order_count` int NOT NULL DEFAULT 0 COMMENT '已完成订单数，退款完成后回减',
  `point_balance` int NOT NULL DEFAULT 0 COMMENT '0至999999，兑换与退款扣回，不得为负',
  `last_buy_time` datetime DEFAULT NULL COMMENT '用于流失预警计算，从未购买时为空',
  `baby_count` int NOT NULL DEFAULT 0 COMMENT '0至5，由宝宝档案有效条数维护',
  `risk_level` varchar(20) NOT NULL DEFAULT '未评估' COMMENT '低风险/中风险/高风险/已流失/未评估',
  `status` varchar(20) NOT NULL DEFAULT '正常' COMMENT '正常/冻结/注销',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除，查询默认过滤',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁，更新时校验并加1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_no` (`member_no`),
  UNIQUE KEY `uk_member_mobile` (`mobile`),
  KEY `idx_level_risk` (`level_code`, `risk_level`),
  KEY `idx_last_buy` (`last_buy_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员';

-- 2. baby_profile 宝宝档案
CREATE TABLE `baby_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `baby_no` varchar(32) NOT NULL COMMENT '规则 BB+会员ID后4位+2位序号，全局唯一',
  `member_id` bigint NOT NULL COMMENT '关联 member.id，一个会员最多5个宝宝',
  `baby_name` varchar(30) NOT NULL COMMENT '1至15字',
  `gender` varchar(10) NOT NULL DEFAULT '未知' COMMENT '男/女/未知',
  `birthday` date NOT NULL COMMENT '不得晚于当前日期，修改需二次确认',
  `month_age` int NOT NULL DEFAULT 0 COMMENT '0至72，每晚按出生日期重算，不允许手工录入',
  `height_cm` decimal(4,1) DEFAULT NULL COMMENT '30.0至130.0',
  `weight_kg` decimal(4,1) DEFAULT NULL COMMENT '1.0至40.0，纸尿裤选码参考',
  `allergy_desc` varchar(200) DEFAULT NULL COMMENT '如牛奶蛋白过敏，最多100字',
  `milk_stage` varchar(10) NOT NULL DEFAULT '未使用' COMMENT '1段/2段/3段/4段/未使用',
  `diaper_size` varchar(10) NOT NULL DEFAULT '未使用' COMMENT 'S/M/L/XL/XXL/未使用',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1有效 0停用，停用后不参与适龄推荐',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除，查询默认过滤',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁，更新时校验并加1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_baby_no` (`baby_no`),
  KEY `idx_member_status` (`member_id`, `status`),
  KEY `idx_month_age` (`month_age`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宝宝档案';

-- 3. baby_goods 母婴商品
CREATE TABLE `baby_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_no` varchar(32) NOT NULL COMMENT '规则 MY+yyMMdd+4位序号，全局唯一',
  `goods_name` varchar(100) NOT NULL COMMENT '2至60字，含品牌与规格',
  `brand` varchar(40) NOT NULL COMMENT '须在品牌字典中存在',
  `goods_type` varchar(20) NOT NULL COMMENT '奶粉/纸尿裤/辅食/洗护/玩具/孕妇用品',
  `age_month_start` int NOT NULL DEFAULT 0 COMMENT '0至72，不得大于适用月龄止',
  `age_month_end` int NOT NULL DEFAULT 72 COMMENT '0至72，与起始月龄共同决定适龄标签',
  `spec_desc` varchar(60) NOT NULL COMMENT '如900g每罐、L码42片',
  `sale_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '0.01至9999.99，后端校验',
  `member_price` decimal(10,2) DEFAULT NULL COMMENT '不得高于售价，为空时按售价结算',
  `stock_qty` int NOT NULL DEFAULT 0 COMMENT '0至9999，下单扣减、取消回补',
  `cover_image` varchar(200) NOT NULL DEFAULT '' COMMENT '商品主图，本地相对路径 /uploads/seed/goods/NN.jpg',
  `status` varchar(20) NOT NULL DEFAULT '待上架' COMMENT '待上架/在售/已售罄/已下架',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除，查询默认过滤',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁，更新时校验并加1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_baby_goods_no` (`goods_no`),
  KEY `idx_type_status` (`goods_type`, `status`),
  KEY `idx_age_range` (`age_month_start`, `age_month_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='母婴商品';

-- 4. coupon 优惠券
CREATE TABLE `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `coupon_no` varchar(32) NOT NULL COMMENT '规则 QY+yyMMdd+4位序号，全局唯一',
  `coupon_name` varchar(60) NOT NULL COMMENT '最多30字，如金卡专享满300减50',
  `coupon_type` varchar(20) NOT NULL COMMENT '满减/折扣/立减/免邮',
  `min_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '0.00至9999.00，满减券必须大于优惠金额',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '满减与立减填金额，折扣券填折扣值',
  `limit_type` varchar(20) NOT NULL DEFAULT '全场通用' COMMENT '全场通用/指定品类/指定商品',
  `limit_value` varchar(60) DEFAULT NULL COMMENT '指定品类的品类名或指定商品ID，全场通用为空',
  `grant_type` varchar(20) NOT NULL COMMENT '主动领取/定向发放/新人礼包/积分兑换',
  `valid_start` datetime NOT NULL COMMENT '与有效期结束成对填写',
  `valid_end` datetime NOT NULL COMMENT '必须晚于开始时间，最长180天',
  `total_qty` int NOT NULL DEFAULT 0 COMMENT '1至100000，后端校验',
  `received_qty` int NOT NULL DEFAULT 0 COMMENT '不得超过发放总量，并发领取需加锁',
  `status` varchar(20) NOT NULL DEFAULT '待发布' COMMENT '待发布/发放中/已停发/已过期',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除，查询默认过滤',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁，更新时校验并加1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_no` (`coupon_no`),
  KEY `idx_type_status` (`coupon_type`, `status`),
  KEY `idx_valid_end` (`valid_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券';

-- 5. coupon_receive 领券记录
CREATE TABLE `coupon_receive` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `receive_no` varchar(32) NOT NULL COMMENT '规则 LQ+yyMMdd+6位序号，全局唯一',
  `coupon_id` bigint NOT NULL COMMENT '关联 coupon.id，同一会员同一券只能一条',
  `member_id` bigint NOT NULL COMMENT '关联 member.id',
  `receive_channel` varchar(20) NOT NULL COMMENT '商城领取/导购发放/新人礼包/积分兑换',
  `receive_time` datetime NOT NULL COMMENT '领取时写入，不可修改',
  `use_status` varchar(20) NOT NULL DEFAULT '未使用' COMMENT '未使用/已使用/已过期/已作废',
  `use_order_id` bigint DEFAULT NULL COMMENT '关联 mall_order.id，核销后必填',
  `use_time` datetime DEFAULT NULL COMMENT '核销时写入，与使用订单同时存在',
  `grant_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id，导购发放时必填',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除，查询默认过滤',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁，更新时校验并加1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_receive_no` (`receive_no`),
  UNIQUE KEY `uk_coupon_member` (`coupon_id`, `member_id`),
  KEY `idx_member_status` (`member_id`, `use_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领券记录';

-- 6. mall_order 商城订单
CREATE TABLE `mall_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(32) NOT NULL COMMENT '规则 MO+yyMMdd+6位序号，全局唯一',
  `member_id` bigint NOT NULL COMMENT '关联 member.id，须为正常状态',
  `item_count` int NOT NULL DEFAULT 1 COMMENT '1至20，后端校验',
  `goods_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '会员价或售价乘件数合计',
  `coupon_receive_id` bigint DEFAULT NULL COMMENT '关联 coupon_receive.id，一单限用一张',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '不超过商品金额，券门槛需满足',
  `freight` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '满199免运费，否则8.00',
  `pay_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品金额加运费减优惠，不得为负',
  `gain_point` int NOT NULL DEFAULT 0 COMMENT '实付金额取整，退款完成后按比例扣回',
  `pay_type` varchar(20) NOT NULL COMMENT '微信/支付宝/储值卡，仅登记',
  `status` varchar(20) NOT NULL DEFAULT '待支付' COMMENT '待支付/待发货/已发货/已完成/退货中/已退款/已取消',
  `receiver_name` varchar(40) DEFAULT NULL COMMENT '收货人（下单时快照）',
  `receiver_mobile` varchar(20) DEFAULT NULL COMMENT '收货手机号',
  `receiver_address` varchar(200) DEFAULT NULL COMMENT '收货地址，发货后不可修改',
  `refund_reason` varchar(200) DEFAULT NULL COMMENT '退款/取消原因，必填不少于5字',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除，查询默认过滤',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁，更新时校验并加1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mall_order_no` (`order_no`),
  KEY `idx_member_status` (`member_id`, `status`),
  KEY `idx_status_create` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单';

-- 7. mall_order_item 订单明细（文档未定义，下单必须知道买了什么，属实现必需表）
CREATE TABLE `mall_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '关联 mall_order.id',
  `goods_id` bigint NOT NULL COMMENT '关联 baby_goods.id',
  `goods_name` varchar(100) NOT NULL COMMENT '下单时商品名称快照',
  `spec_desc` varchar(60) NOT NULL COMMENT '下单时规格快照',
  `price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '成交单价（会员价优先）',
  `qty` int NOT NULL DEFAULT 1 COMMENT '数量1至20',
  `amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价乘数量',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

-- 8. point_log 积分流水
CREATE TABLE `point_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `point_no` varchar(32) NOT NULL COMMENT '规则 JF+yyMMdd+6位序号，全局唯一',
  `member_id` bigint NOT NULL COMMENT '关联 member.id',
  `change_type` varchar(20) NOT NULL COMMENT '下单获得/评价获得/签到/兑换扣减/退款扣回/人工调整',
  `change_point` int NOT NULL DEFAULT 0 COMMENT '正数增加、负数扣减，不得为0',
  `before_point` int NOT NULL DEFAULT 0 COMMENT '0至999999',
  `after_point` int NOT NULL DEFAULT 0 COMMENT '等于变动前积分加变动值，不得为负',
  `biz_no` varchar(32) DEFAULT NULL COMMENT '关联订单号或兑换单号',
  `remark` varchar(200) DEFAULT NULL COMMENT '人工调整时必填，不少于5字',
  `operator_id` bigint DEFAULT NULL COMMENT '关联 sys_user.id，系统自动时为空',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除，查询默认过滤',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁，更新时校验并加1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_point_no` (`point_no`),
  KEY `idx_member_create` (`member_id`, `create_time`),
  KEY `idx_biz_no` (`biz_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水';

-- 9. repurchase_task 复购提醒
CREATE TABLE `repurchase_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `remind_no` varchar(32) NOT NULL COMMENT '规则 FG+yyMMdd+4位序号，全局唯一',
  `member_id` bigint NOT NULL COMMENT '关联 member.id，同一商品7天内只一条',
  `goods_id` bigint NOT NULL COMMENT '关联 baby_goods.id，须为奶粉或纸尿裤类',
  `last_buy_date` date NOT NULL COMMENT '取该商品最近一次已完成订单',
  `predict_end_date` date NOT NULL COMMENT '按规格与日均用量估算，不得早于上次购买日期',
  `remind_channel` varchar(20) NOT NULL COMMENT '站内消息/短信模拟/导购电话',
  `plan_time` datetime NOT NULL COMMENT '预计用完日期前3天，不得早于当前日期',
  `send_status` varchar(20) NOT NULL DEFAULT '待发送' COMMENT '待发送/已发送/已跳过/发送失败',
  `response_result` varchar(20) NOT NULL DEFAULT '无响应' COMMENT '已复购/咨询未购/无响应/退订',
  `owner_id` bigint DEFAULT NULL COMMENT '关联 sys_user.id，导购跟进时填写',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除，查询默认过滤',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁，更新时校验并加1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_remind_no` (`remind_no`),
  KEY `idx_member_goods` (`member_id`, `goods_id`),
  KEY `idx_plan_status` (`plan_time`, `send_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复购提醒';

-- 10. goods_review 商品评价（文档角色职责要求「审核商品评价」，功能章节未展开，按最小闭环补建）
CREATE TABLE `goods_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '关联 mall_order.id，一单一评',
  `goods_id` bigint NOT NULL COMMENT '关联 baby_goods.id',
  `member_id` bigint NOT NULL COMMENT '关联 member.id',
  `score` tinyint NOT NULL DEFAULT 5 COMMENT '1至5星',
  `content` varchar(500) NOT NULL COMMENT '评价内容，5至500字',
  `images` varchar(500) DEFAULT NULL COMMENT '晒图相对路径，逗号分隔，最多3张',
  `reply` varchar(300) DEFAULT NULL COMMENT '门店回复',
  `status` varchar(20) NOT NULL DEFAULT '待审核' COMMENT '待审核/已通过/已驳回',
  `audit_by` bigint DEFAULT NULL COMMENT '审核人 sys_user.id',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除，查询默认过滤',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁，更新时校验并加1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_order` (`order_id`),
  KEY `idx_goods_status` (`goods_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价';

-- 11. churn_rule 流失规则版本（创新点 1：规则配置 + 版本留痕）
CREATE TABLE `churn_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_name` varchar(60) NOT NULL COMMENT '规则名称',
  `rule_version` varchar(20) NOT NULL COMMENT '版本号 v1/v2/v3，全局唯一',
  `weight_gap` int NOT NULL DEFAULT 35 COMMENT '最近购买间隔权重，合计须为100',
  `weight_freq` int NOT NULL DEFAULT 25 COMMENT '近90天消费频次权重',
  `weight_amount` int NOT NULL DEFAULT 20 COMMENT '客单价变化率权重',
  `weight_coupon` int NOT NULL DEFAULT 10 COMMENT '优惠券使用率权重',
  `weight_age` int NOT NULL DEFAULT 10 COMMENT '宝宝月龄阶段跨度权重',
  `high_threshold` int NOT NULL DEFAULT 75 COMMENT '高风险阈值，风险分大于等于此值判高风险',
  `mid_threshold` int NOT NULL DEFAULT 50 COMMENT '中风险阈值',
  `status` varchar(20) NOT NULL DEFAULT '草稿' COMMENT '草稿/生效/历史',
  `effect_time` datetime NOT NULL COMMENT '生效时间，历史版本保留不覆盖',
  `remark` varchar(200) DEFAULT NULL COMMENT '变更说明，不少于5字',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_version` (`rule_version`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流失规则版本';

-- 12. churn_score 流失计算快照（创新点 2：输入快照 + 各因素得分 + 总分 + 结论）
CREATE TABLE `churn_score` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id` bigint NOT NULL COMMENT '关联 member.id',
  `rule_id` bigint NOT NULL COMMENT '关联 churn_rule.id',
  `rule_version` varchar(20) NOT NULL COMMENT '计算时使用的规则版本',
  `input_json` varchar(1000) NOT NULL COMMENT '输入快照 JSON，保证同输入同结果',
  `score_gap` int NOT NULL DEFAULT 0 COMMENT '最近购买间隔得分',
  `score_freq` int NOT NULL DEFAULT 0 COMMENT '消费频次得分',
  `score_amount` int NOT NULL DEFAULT 0 COMMENT '客单价变化得分',
  `score_coupon` int NOT NULL DEFAULT 0 COMMENT '券使用率得分',
  `score_age` int NOT NULL DEFAULT 0 COMMENT '月龄跨度得分',
  `total_score` decimal(5,1) NOT NULL DEFAULT 0.0 COMMENT '加权总分，越高流失风险越大',
  `risk_level` varchar(20) NOT NULL DEFAULT '低风险' COMMENT '低风险/中风险/高风险/已流失',
  `conclusion` varchar(300) DEFAULT NULL COMMENT '推荐结论，展示主要影响因素',
  `unmet` varchar(300) DEFAULT NULL COMMENT '未满足条件/数据缺口说明',
  `suggest` varchar(300) DEFAULT NULL COMMENT '可改进项与召回建议',
  `owner_id` bigint DEFAULT NULL COMMENT '分配的导购 sys_user.id',
  `recall_status` varchar(20) NOT NULL DEFAULT '无需召回' COMMENT '待召回/跟进中/已召回/无需召回',
  `calc_time` datetime NOT NULL COMMENT '计算时间',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  KEY `idx_member_calc` (`member_id`, `calc_time`),
  KEY `idx_level_score` (`risk_level`, `total_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流失计算快照';

-- 13. churn_adjust 人工修正（创新点 4：原始结果保留，修正留痕）
CREATE TABLE `churn_adjust` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `score_id` bigint NOT NULL COMMENT '关联 churn_score.id，原始快照不被覆盖',
  `member_id` bigint NOT NULL COMMENT '关联 member.id',
  `origin_level` varchar(20) NOT NULL COMMENT '修正前等级',
  `origin_score` decimal(5,1) NOT NULL COMMENT '修正前分数',
  `new_level` varchar(20) NOT NULL COMMENT '修正后等级',
  `reason` varchar(200) NOT NULL COMMENT '修正原因，不少于5字',
  `adjust_by` bigint NOT NULL COMMENT '修正人 sys_user.id',
  `adjust_time` datetime NOT NULL COMMENT '修正时间',
  `create_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '关联 sys_user.id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时自动刷新',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  KEY `idx_score` (`score_id`),
  KEY `idx_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流失人工修正';
""")

DDL.append("""
-- 14. 通用表：用户
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(40) NOT NULL COMMENT '登录账号，唯一',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt 密文',
  `real_name` varchar(40) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(80) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像相对路径',
  `credit_score` int NOT NULL DEFAULT 100 COMMENT '信用分，违规扣分用',
  `dept_id` bigint DEFAULT NULL COMMENT '所属门店/部门',
  `dept_name` varchar(60) DEFAULT NULL COMMENT '门店名称冗余，列表直接展示',
  `status` varchar(20) NOT NULL DEFAULT '正常' COMMENT '正常/停用',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` varchar(40) NOT NULL COMMENT '角色名称',
  `role_code` varchar(40) NOT NULL COMMENT '角色编码，唯一',
  `data_scope` tinyint NOT NULL DEFAULT 1 COMMENT '1本人 2本部门 3全部',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '角色列表排序',
  `status` varchar(20) NOT NULL DEFAULT '正常' COMMENT '正常/停用',
  `remark` varchar(200) DEFAULT NULL COMMENT '职责说明，与需求文档第 2 节一致',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户',
  `role_id` bigint NOT NULL COMMENT '角色',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色表';

CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级，0 为顶级',
  `menu_name` varchar(40) NOT NULL COMMENT '菜单名称',
  `path` varchar(120) DEFAULT NULL COMMENT '前端路由',
  `component` varchar(120) DEFAULT NULL COMMENT '组件标识',
  `perms` varchar(80) DEFAULT NULL COMMENT '权限标识',
  `type` char(1) NOT NULL DEFAULT 'C' COMMENT 'M目录 C菜单 F按钮',
  `icon` varchar(40) DEFAULT NULL COMMENT '图标名',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '1显示 0隐藏',
  `status` varchar(20) NOT NULL DEFAULT '正常' COMMENT '正常/停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色',
  `menu_id` bigint NOT NULL COMMENT '菜单',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单表';

CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_name` varchar(60) NOT NULL COMMENT '字典名称',
  `dict_type` varchar(60) NOT NULL COMMENT '字典类型，唯一',
  `status` varchar(20) NOT NULL DEFAULT '正常' COMMENT '正常/停用',
  `remark` varchar(200) DEFAULT NULL COMMENT '说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type` varchar(60) NOT NULL COMMENT '字典类型',
  `dict_label` varchar(60) NOT NULL COMMENT '显示值',
  `dict_value` varchar(60) NOT NULL COMMENT '存储值',
  `tag_type` varchar(20) DEFAULT NULL COMMENT '列表标签配色 success/warning/danger/info',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` varchar(20) NOT NULL DEFAULT '正常' COMMENT '正常/停用',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_name` varchar(120) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(200) NOT NULL COMMENT '相对路径，不存外链',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '字节',
  `file_type` varchar(40) NOT NULL COMMENT '扩展名',
  `biz_type` varchar(40) DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint DEFAULT NULL COMMENT '业务主键',
  `uploader_id` bigint DEFAULT NULL COMMENT '上传人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(120) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '正文',
  `notice_type` varchar(20) NOT NULL DEFAULT '门店公告' COMMENT '门店公告/活动通知/系统通知',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  `status` varchar(20) NOT NULL DEFAULT '已发布' COMMENT '草稿/已发布/已下线',
  `publisher_id` bigint DEFAULT NULL COMMENT '发布人 sys_user.id',
  `create_by` bigint DEFAULT NULL COMMENT '发布人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE TABLE `sys_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `receiver_id` bigint NOT NULL COMMENT '接收人 sys_user.id',
  `title` varchar(120) NOT NULL COMMENT '标题',
  `content` varchar(500) NOT NULL COMMENT '内容',
  `biz_type` varchar(40) DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint DEFAULT NULL COMMENT '业务主键',
  `msg_level` varchar(20) NOT NULL DEFAULT '普通' COMMENT '普通/重要/紧急',
  `read_flag` tinyint NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_id`, `read_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `module` varchar(60) NOT NULL COMMENT '模块',
  `oper_type` varchar(40) NOT NULL COMMENT '新增/修改/删除/导出/流转',
  `biz_no` varchar(40) DEFAULT NULL COMMENT '业务单号',
  `content` varchar(500) DEFAULT NULL COMMENT '操作内容',
  `oper_id` bigint DEFAULT NULL COMMENT '操作人',
  `oper_name` varchar(40) DEFAULT NULL COMMENT '操作人姓名冗余',
  `oper_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `ip` varchar(40) DEFAULT NULL COMMENT '来源 IP',
  PRIMARY KEY (`id`),
  KEY `idx_time` (`oper_time`),
  KEY `idx_oper` (`oper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
SET FOREIGN_KEY_CHECKS = 1;
""")

# ---------------------------------------------------------------- 数据 SQL
DATA = []

# 角色
DATA.append(rows("sys_role", ["id", "role_name", "role_code", "data_scope", "status", "remark", "create_time"], [
    (1, "系统管理员", "ADMIN", 3, "正常", "管理用户、角色、菜单、字典、参数和操作日志", dt_s(NOW - timedelta(days=200))),
    (2, "门店店长", "MANAGER", 2, "正常", "审核券预算与发券量，查看门店会员分层与业绩", dt_s(NOW - timedelta(days=200))),
    (3, "商城运营", "OPERATOR", 3, "正常", "维护商品与适龄标签、配置优惠券活动、审核商品评价", dt_s(NOW - timedelta(days=200))),
    (4, "会员营销专员", "MARKETER", 3, "正常", "查看流失预警名单、发起复购提醒与定向发券召回", dt_s(NOW - timedelta(days=200))),
    (5, "门店导购", "GUIDE", 1, "正常", "维护会员与宝宝档案、推荐适龄商品、登记线下咨询与跟进结果", dt_s(NOW - timedelta(days=200))),
    (6, "会员家长", "MEMBER", 1, "正常", "完善宝宝档案、领取优惠券、下单支付、查看积分与复购提醒", dt_s(NOW - timedelta(days=200))),
]))

# 用户：每角色至少 2 个
users = [
    (1, "admin", "系统管理员", "13800000001", None, 100, "滨江星光店", "正常"),
    (2, "admin2", "赵敏", "13800000002", None, 100, "滨江星光店", "正常"),
    (3, "manager01", "周敏", "13800000012", "/uploads/seed/avatar/01.jpg", 100, "滨江星光店", "正常"),
    (4, "manager02", "陆晨", "13800000013", "/uploads/seed/avatar/02.jpg", 101, "城西银泰店", "正常"),
    (5, "operator", "许静", "13800000023", "/uploads/seed/avatar/03.jpg", 100, "滨江星光店", "正常"),
    (6, "operator02", "方蕾", "13800000024", "/uploads/seed/avatar/04.jpg", 100, "滨江星光店", "正常"),
    (7, "marketer01", "顾晓", "13800000034", "/uploads/seed/avatar/05.jpg", 100, "滨江星光店", "正常"),
    (8, "marketer02", "沈亦", "13800000035", "/uploads/seed/avatar/06.jpg", 100, "滨江星光店", "正常"),
    (9, "guide01", "林小满", "13800000045", "/uploads/seed/avatar/07.jpg", 100, "滨江星光店", "正常"),
    (10, "guide02", "王璐", "13800000046", "/uploads/seed/avatar/08.jpg", 100, "滨江星光店", "正常"),
    (11, "guide03", "陈曦", "13800000047", "/uploads/seed/avatar/01.jpg", 101, "城西银泰店", "正常"),
    (12, "user", "王雅琴", "13800002210", None, 100, "滨江星光店", "正常"),
    (13, "user2", "李思远", "13800002211", None, 100, "滨江星光店", "正常"),
]
DATA.append(rows("sys_user", ["id", "username", "password", "real_name", "phone", "avatar", "dept_id", "dept_name", "status", "create_time"],
                 [(u[0], u[1], PWD, u[2], u[3], u[4], u[5], u[6], u[7], dt_s(NOW - timedelta(days=190 + u[0]))) for u in users]))
DATA.append(rows("sys_user_role", ["user_id", "role_id"],
                 [(u[0], {1: 1, 2: 1, 3: 2, 4: 2, 5: 3, 6: 3, 7: 4, 8: 4, 9: 5, 10: 5, 11: 5, 12: 6, 13: 6}[u[0]]) for u in users]))

# 菜单
MENUS = [
    # (id, parent, name, path, component, perms, type, icon, sort, visible)
    (1, 0, "经营概览", None, None, None, "M", "DataLine", 10, 1),
    (2, 0, "会员经营", None, None, None, "M", "User", 20, 1),
    (3, 0, "商品中心", None, None, None, "M", "Goods", 30, 1),
    (4, 0, "营销中心", None, None, None, "M", "Present", 40, 1),
    (5, 0, "订单中心", None, None, None, "M", "List", 50, 1),
    (6, 0, "系统管理", None, None, None, "M", "Setting", 60, 1),
    (7, 0, "会员门户", None, None, None, "M", "Shop", 70, 1),

    (101, 1, "经营驾驶舱", "/dashboard", "Dashboard", "dashboard:view", "C", "DataLine", 1, 1),
    (201, 2, "会员档案", "/member", "CrudView", "member:view", "C", "User", 1, 1),
    (202, 2, "宝宝档案", "/baby", "CrudView", "baby:view", "C", "Avatar", 2, 1),
    (203, 2, "会员分层看板", "/churn", "Churn", "churn:view", "C", "Warning", 3, 1),
    (204, 2, "流失规则配置", "/churn-rule", "ChurnRule", "churn:rule", "C", "SetUp", 4, 1),
    (301, 3, "商品管理", "/goods", "CrudView", "goods:view", "C", "Goods", 1, 1),
    (302, 3, "评价审核", "/review", "CrudView", "review:view", "C", "ChatDotSquare", 2, 1),
    (401, 4, "优惠券配置", "/coupon", "CrudView", "coupon:view", "C", "Ticket", 1, 1),
    (402, 4, "发券任务", "/grant", "Grant", "coupon:grant", "C", "Promotion", 2, 1),
    (403, 4, "复购提醒台", "/repurchase", "Repurchase", "repurchase:view", "C", "Bell", 3, 1),
    (404, 4, "营销报表", "/report", "Report", "report:view", "C", "TrendCharts", 4, 1),
    (501, 5, "订单管理", "/order", "CrudView", "order:view", "C", "List", 1, 1),
    (601, 6, "用户管理", "/user", "CrudView", "user:view", "C", "UserFilled", 1, 1),
    (602, 6, "角色管理", "/role", "RoleManage", "role:view", "C", "Avatar", 2, 1),
    (603, 6, "菜单管理", "/menu", "MenuManage", "menu:view", "C", "Menu", 3, 1),
    (604, 6, "字典管理", "/dict", "DictManage", "dict:view", "C", "Collection", 4, 1),
    (605, 6, "操作日志", "/oper-log", "CrudView", "operlog:view", "C", "Document", 5, 1),
    (701, 7, "商城首页", "/portal/mall", "portal/Mall", "portal:mall", "C", "Shop", 1, 1),
    (702, 7, "我的中心", "/portal/my", "portal/My", "portal:my", "C", "House", 2, 1),

    # 按钮权限
    (1001, 201, "会员新增", None, None, "member:add", "F", None, 1, 1),
    (1002, 201, "会员编辑", None, None, "member:edit", "F", None, 2, 1),
    (1003, 201, "会员删除", None, None, "member:delete", "F", None, 3, 1),
    (1004, 201, "等级重算", None, None, "member:recalc", "F", None, 4, 1),
    (1005, 203, "人工修正", None, None, "churn:adjust", "F", None, 5, 1),
    (1006, 203, "分配导购", None, None, "churn:assign", "F", None, 6, 1),
    (1007, 401, "新建券", None, None, "coupon:add", "F", None, 7, 1),
    (1008, 402, "定向发券", None, None, "coupon:grantDo", "F", None, 8, 1),
    (1009, 403, "生成提醒", None, None, "repurchase:generate", "F", None, 9, 1),
    (1010, 403, "登记跟进", None, None, "repurchase:follow", "F", None, 10, 1),
    (1011, 501, "订单发货", None, None, "order:ship", "F", None, 11, 1),
    (1012, 501, "订单取消", None, None, "order:cancel", "F", None, 12, 1),
    (1013, 302, "评价审核", None, None, "review:audit", "F", None, 13, 1),
]
DATA.append(rows("sys_menu", ["id", "parent_id", "menu_name", "path", "component", "perms", "type", "icon", "sort_no", "visible"],
                 [(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9]) for m in MENUS]))

# 角色菜单：ADMIN 全量；店长看经营/会员/订单/报表；运营看商品/券；营销看分层/发券/复购/报表；导购看会员/宝宝/复购
ROLE_MENUS = {1: [m[0] for m in MENUS]}
ROLE_MENUS[2] = [1, 2, 4, 5, 101, 201, 202, 203, 403, 404, 501, 1005, 1006, 1011, 1012, 1009, 1010]
ROLE_MENUS[3] = [1, 3, 4, 5, 101, 301, 302, 401, 402, 501, 1007, 1008, 1013, 1011]
ROLE_MENUS[4] = [1, 2, 4, 101, 201, 203, 204, 401, 402, 403, 404, 1005, 1006, 1007, 1008, 1009, 1010]
ROLE_MENUS[5] = [2, 5, 201, 202, 203, 403, 1001, 1002, 1005, 1009, 1010]
ROLE_MENUS[6] = [7, 701, 702]
rm = []
for rid_, mids in ROLE_MENUS.items():
    for mid in mids:
        rm.append((rid_, mid))
DATA.append(rows("sys_role_menu", ["role_id", "menu_id"], rm))

# 字典
DICTS = [
    ("会员等级", "member_level", ["普通会员", "银卡", "金卡", "黑卡"]),
    ("注册渠道", "register_channel", ["门店扫码", "小程序", "导购推荐"]),
    ("会员状态", "member_status", ["正常", "冻结", "注销"]),
    ("流失风险", "risk_level", ["低风险", "中风险", "高风险", "已流失", "未评估"]),
    ("商品品类", "goods_type", ["奶粉", "纸尿裤", "辅食", "洗护", "玩具", "孕妇用品"]),
    ("商品状态", "goods_status", ["待上架", "在售", "已售罄", "已下架"]),
    ("券类型", "coupon_type", ["满减", "折扣", "立减", "免邮"]),
    ("券状态", "coupon_status", ["待发布", "发放中", "已停发", "已过期"]),
    ("发放方式", "grant_type", ["主动领取", "定向发放", "新人礼包", "积分兑换"]),
    ("适用范围", "limit_type", ["全场通用", "指定品类", "指定商品"]),
    ("领取状态", "use_status", ["未使用", "已使用", "已过期", "已作废"]),
    ("订单状态", "order_status", ["待支付", "待发货", "已发货", "已完成", "退货中", "已退款", "已取消"]),
    ("支付方式", "pay_type", ["微信", "支付宝", "储值卡"]),
    ("积分类型", "point_type", ["下单获得", "评价获得", "签到", "兑换扣减", "退款扣回", "人工调整"]),
    ("提醒方式", "remind_channel", ["站内消息", "短信模拟", "导购电话"]),
    ("发送状态", "send_status", ["待发送", "已发送", "已跳过", "发送失败"]),
    ("会员响应", "response_result", ["已复购", "咨询未购", "无响应", "退订"]),
    ("召回状态", "recall_status", ["待召回", "跟进中", "已召回", "无需召回"]),
    ("奶粉段位", "milk_stage", ["1段", "2段", "3段", "4段", "未使用"]),
    ("纸尿裤尺码", "diaper_size", ["S", "M", "L", "XL", "XXL", "未使用"]),
    ("宝宝性别", "gender", ["男", "女", "未知"]),
    ("评价状态", "review_status", ["待审核", "已通过", "已驳回"]),
    ("规则状态", "rule_status", ["草稿", "生效", "历史"]),
    ("数据范围", "data_scope", ["本人", "本部门", "全部"]),
]
dt_rows = []
dd_rows = []
for i, (nm, typ, ditem) in enumerate(DICTS, 1):
    dt_rows.append((i, nm, typ, "正常", None))
    for j, it in enumerate(ditem, 1):
        dd_rows.append((len(dd_rows) + 1, typ, it, it, j * 10, "正常"))
DATA.append(rows("sys_dict_type", ["id", "dict_name", "dict_type", "status", "remark"], dt_rows))
DATA.append(rows("sys_dict_data", ["id", "dict_type", "dict_label", "dict_value", "sort_no", "status"], dd_rows))

# 会员
DATA.append(rows("member", ["id", "member_no", "mobile", "nick_name", "level_code", "register_channel",
                            "total_amount", "order_count", "point_balance", "last_buy_time", "baby_count",
                            "risk_level", "status", "create_time", "update_time"],
                 [(m["id"], m["no"], m["mobile"], m["nick"], lvl, m["channel"], round(m["total"], 2), m["cnt"],
                   m["point"], dt_s(m["last_buy"]) if m["last_buy"] else None, m["baby_count"], m["risk"],
                   m["status"], dt_s(m["created"]), dt_s(m["created"]))
                  for m, lvl in zip(members, [
                      m.get("level") or ("黑卡" if m["total"] >= 8000 else "金卡" if m["total"] >= 3000 else
                      "银卡" if m["total"] >= 1000 else "普通会员") for m in members])]))

# 宝宝
DATA.append(rows("baby_profile", ["id", "baby_no", "member_id", "baby_name", "gender", "birthday", "month_age",
                                  "height_cm", "weight_kg", "allergy_desc", "milk_stage", "diaper_size", "status",
                                  "create_time", "update_time"],
                 [(b["id"], b["no"], b["mid"], b["name"], b["gender"], d(b["birthday"]), b["month"], b["h"], b["w"],
                   b["allergy"], b["stage"], b["size"], b["status"], dt_s(b["created"]), dt_s(b["created"]))
                  for b in babies]))

# 商品
DATA.append(rows("baby_goods", ["id", "goods_no", "goods_name", "brand", "goods_type", "age_month_start", "age_month_end",
                                "spec_desc", "sale_price", "member_price", "stock_qty", "cover_image", "status",
                                "create_time", "update_time"],
                 [(g["id"], g["no"], g["name"], g["brand"], g["type"], g["a0"], g["a1"], g["spec"], g["sp"], g["mp"],
                   g["stock"], g["img"], g["status"],
                   dt_s(NOW - timedelta(days=random.randint(20, 170))), dt_s(NOW - timedelta(days=random.randint(1, 18))))
                  for g in goods]))

# 券
DATA.append(rows("coupon", ["id", "coupon_no", "coupon_name", "coupon_type", "min_amount", "discount_amount",
                            "limit_type", "limit_value", "grant_type", "valid_start", "valid_end", "total_qty",
                            "received_qty", "status", "create_time"],
                 [(c["id"], c["no"], c["name"], c["type"], c["min"], c["disc"], c["limit"],
                   "纸尿裤" if c["name"].startswith("纸尿裤") else ("奶粉" if c["name"].startswith("奶粉") else
                                                            ("辅食" if c["name"].startswith("辅食") else
                                                             ("洗护" if "洗护" in c["name"] else
                                                              ("玩具" if c["name"].startswith("玩具") else
                                                               ("孕妇" if c["name"].startswith("孕妇") else None))))),
                   c["grant"], dt_s(c["vs"]), dt_s(c["ve"]), c["total"], c["recv"], c["status"], dt_s(c["created"]))
                  for c in coupons]))

# 领券
DATA.append(rows("coupon_receive", ["id", "receive_no", "coupon_id", "member_id", "receive_channel", "receive_time",
                                    "use_status", "use_order_id", "use_time", "grant_by", "create_time"],
                 [(r["id"], r["no"], r["cid"], r["mid"], r["channel"], dt_s(r["time"]), r["use"], r["oid"],
                   dt_s(r["utime"]) if r["utime"] else None, r["grant"], dt_s(r["time"])) for r in receives]))

# 订单
DATA.append(rows("mall_order", ["id", "order_no", "member_id", "item_count", "goods_amount", "coupon_receive_id",
                                "discount_amount", "freight", "pay_amount", "gain_point", "pay_type", "status",
                                "receiver_name", "receiver_mobile", "receiver_address", "refund_reason", "create_time", "update_time"],
                 [(o["id"], o["no"], o["mid"], o["cnt"], o["g_amount"], o["rid"], o["disc"], o["freight"], o["pay"],
                   o["gain"], o["pay_type"], o["status"],
                   members[o["mid"] - 1]["nick"], members[o["mid"] - 1]["mobile"],
                   "杭州市滨江区星光大道 %d 号 %d 幢 %d0%02d 室" % (100 + o["mid"], 1 + o["mid"] % 5, 1 + o["mid"] % 9, 1 + o["mid"] % 20),
                   ("缺货，与会员协商整单取消" if o["status"] == "已取消" else
                    ("尺码不合适，已寄回门店" if o["status"] == "退货中" else
                     ("商品破损，已退款" if o["status"] == "已退款" else None))),
                   dt_s(o["created"]), dt_s(o["created"] + timedelta(minutes=random.randint(5, 900))))
                  for o in orders]))

DATA.append(rows("mall_order_item", ["id", "order_id", "goods_id", "goods_name", "spec_desc", "price", "qty", "amount", "create_time"],
                 [(it["id"], it["oid"], it["gid"], it["name"], it["spec"], it["price"], it["qty"], it["amount"], dt_s(NOW)) for it in items]))

# 积分
DATA.append(rows("point_log", ["id", "point_no", "member_id", "change_type", "change_point", "before_point",
                               "after_point", "biz_no", "remark", "operator_id", "create_time"],
                 [(p["id"], p["no"], p["mid"], p["type"], p["chg"], p["before"], p["after"], p["biz"], p["remark"],
                   p["op"], dt_s(p["created"])) for p in point_logs]))

# 复购
DATA.append(rows("repurchase_task", ["id", "remind_no", "member_id", "goods_id", "last_buy_date", "predict_end_date",
                                     "remind_channel", "plan_time", "send_status", "response_result", "owner_id", "create_time"],
                 [(r["id"], r["no"], r["mid"], r["gid"], r["last"], r["end"], r["channel"], dt_s(r["plan"]),
                   r["send"], r["resp"], r["owner"], dt_s(r["created"])) for r in repurchase]))

# 评价
DATA.append(rows("goods_review", ["id", "order_id", "goods_id", "member_id", "score", "content", "images", "status",
                                  "audit_by", "audit_time", "create_time"],
                 [(r["id"], r["oid"], r["gid"], r["mid"], r["score"], r["content"],
                   "/uploads/seed/goods/%02d.jpg" % GOODS_IMG[r["gid"] - 1] if r["id"] % 3 == 0 else None,
                   r["status"], 5 if r["status"] != "待审核" else None,
                   dt_s(r["created"] + timedelta(days=1)) if r["status"] != "待审核" else None,
                   dt_s(r["created"])) for r in reviews]))

# 规则
DATA.append(rows("churn_rule", ["id", "rule_name", "rule_version", "weight_gap", "weight_freq", "weight_amount",
                                "weight_coupon", "weight_age", "high_threshold", "mid_threshold", "status",
                                "effect_time", "remark", "create_time"],
                 [(r["id"], r["name"], r["ver"], r["w"][0], r["w"][1], r["w"][2], r["w"][3], r["w"][4],
                   r["hi"], r["mid"], r["status"], dt_s(r["eff"]), r["remark"], dt_s(r["eff"])) for r in rules]))

# 流失快照
DATA.append(rows("churn_score", ["id", "member_id", "rule_id", "rule_version", "input_json", "score_gap", "score_freq",
                                 "score_amount", "score_coupon", "score_age", "total_score", "risk_level", "conclusion",
                                 "unmet", "suggest", "owner_id", "recall_status", "calc_time"],
                 [(s["id"], s["mid"], s["rule"], s["ver"], s["inp"], s["s"][0], s["s"][1], s["s"][2], s["s"][3], s["s"][4],
                   s["total"], s["lvl"], s["concl"], s["unmet"], s["suggest"], s["owner"], s["recall"], dt_s(s["calc"]))
                  for s in scores]))

# 修正
DATA.append(rows("churn_adjust", ["id", "score_id", "member_id", "origin_level", "origin_score", "new_level",
                                  "reason", "adjust_by", "adjust_time"],
                 [(i, a[0], a[1], a[2], float(scores[a[0] - 1]["total"]), a[3], a[4], 4, dt_s(NOW - timedelta(days=i))) 
                  for i, a in enumerate(adjusts, 1)]))

# 公告
DATA.append(rows("sys_notice", ["id", "title", "content", "publish_time", "status", "create_by", "create_time"], [
    (1, "秋冬纸尿裤囤货季开始", "金卡会员满 300 减 50，L/XL 码纸尿裤叠加券后低至 122 元/包，活动截止 11 月 30 日。", dt_s(NOW - timedelta(days=3)), "已发布", 3, dt_s(NOW - timedelta(days=3))),
    (2, "本月会员积分兑换活动", "新增「积分兑纸尿裤试用装」，2000 积分即可兑换，用于消化积分沉淀。", dt_s(NOW - timedelta(days=8)), "已发布", 3, dt_s(NOW - timedelta(days=8))),
    (3, "流失会员召回专项启动", "37 名高风险会员需在 48 小时内完成电话召回，请各位导购在复购提醒台登记跟进结果。", dt_s(NOW - timedelta(days=1)), "已发布", 7, dt_s(NOW - timedelta(days=1))),
]))

# 站内消息
msgs = []
for i in range(1, 13):
    msgs.append((i, 7 + (i % 2), "待召回会员提醒", "会员 %s 风险分 %d，请在 48 小时内跟进。" % (members[i]["nick"], 70 + i),
                 "churn", i, 1 if i % 3 == 0 else 0, dt_s(NOW - timedelta(days=i))))
for i in range(13, 19):
    msgs.append((i, 9, "复购提醒待发送", "有 %d 条复购提醒将在今天 10:00 发送，请确认名单。" % (3 + i % 5),
                 "repurchase", i, 0, dt_s(NOW - timedelta(hours=i))))
DATA.append(rows("sys_message", ["id", "receiver_id", "title", "content", "biz_type", "biz_id", "read_flag", "create_time"], msgs))

# 操作日志
logs = []
for i in range(1, 26):
    logs.append((i, ["会员档案", "订单管理", "优惠券配置", "会员分层看板", "商品管理"][i % 5],
                 ["新增", "修改", "流转", "发券", "导出"][i % 5],
                 "MO26%04d%06d" % (1000 + i, 100000 + i) if i % 2 else "HY26%04d" % (1000 + i),
                 "提交 %s 操作，影响 1 条记录" % ["新增", "修改", "流转", "发券", "导出"][i % 5],
                 1 + i % 12, users[i % 12][2], dt_s(NOW - timedelta(hours=i * 5)), "127.0.0.1"))
DATA.append(rows("sys_oper_log", ["id", "module", "oper_type", "biz_no", "content", "oper_id", "oper_name", "oper_time", "ip"], logs))

# 文件表
DATA.append(rows("sys_file", ["id", "file_name", "file_path", "file_size", "file_type", "biz_type", "biz_id", "uploader_id", "create_time"],
                 [(i, "商品主图-%02d.jpg" % i, "/uploads/seed/goods/%02d.jpg" % GOODS_IMG[i - 1], 180000 + i * 1024, "jpg", "goods", i, 5,
                   dt_s(NOW - timedelta(days=30 + i))) for i in range(1, 17)]))

# ---------------------------------------------------------------- 输出
with open(os.path.join(BASE, "init.sql"), "w", encoding="utf-8") as f:
    f.write("\n\n".join(DDL) + "\n\n-- ================== 初始化数据 ==================\n\n")
    f.write("\n\n".join(DATA) + "\n")
print("init.sql 已生成：会员 %d / 宝宝 %d / 商品 %d / 券 %d / 领券 %d / 订单 %d / 明细 %d / 积分 %d / 复购 %d / 评价 %d / 流失分 %d"
      % (len(members), len(babies), len(goods), len(coupons), len(receives), len(orders), len(items),
         len(point_logs), len(repurchase), len(reviews), len(scores)))
