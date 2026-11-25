#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 design/ 下的可演示 HTML mock（内置假数据、可点击跳转）。
运行：python3 design/build_mocks.py
输出：design/*.html + design/assets/style.css + design/icons.svg
"""
import os

BASE = os.path.dirname(os.path.abspath(__file__))
os.makedirs(os.path.join(BASE, "assets"), exist_ok=True)

CSS = """
:root{
  --primary:#C86B85; --primary-2:#E3A0B4; --accent:#7FB3A6; --bg:#F5F6F7;
  --ink:#1F2933; --muted:#5B6770; --ok:#2B7955; --danger:#B02A37;
  --line:#E7E4E6; --card:#FFFFFF; --soft:#FBF3F5; --radius:14px;
  --shadow:0 1px 2px rgba(31,41,51,.04),0 8px 24px rgba(31,41,51,.05);
}
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:"PingFang SC","Microsoft YaHei",system-ui,-apple-system,sans-serif;background:var(--bg);color:var(--ink);font-size:14px;line-height:1.6;-webkit-font-smoothing:antialiased}
a{color:inherit;text-decoration:none}
.app{display:flex;min-height:100vh}
/* ---- 侧边栏 220px ---- */
.side{width:220px;flex:0 0 220px;background:#fff;border-right:1px solid var(--line);position:sticky;top:0;height:100vh;display:flex;flex-direction:column}
.brand{height:56px;display:flex;align-items:center;gap:10px;padding:0 18px;border-bottom:1px solid var(--line);font-weight:700;font-size:15px;letter-spacing:.3px}
.brand .logo{width:26px;height:26px;border-radius:9px;background:linear-gradient(135deg,var(--primary),var(--primary-2));display:flex;align-items:center;justify-content:center;color:#fff;font-size:13px}
.brand small{display:block;font-size:9px;color:var(--muted);font-weight:400;letter-spacing:1.4px}
.nav{flex:1;overflow:auto;padding:12px 10px}
.grp{font-size:11px;color:#9AA4AC;padding:12px 8px 6px;letter-spacing:.8px}
.item{display:flex;align-items:center;gap:9px;padding:9px 12px;border-radius:9px;color:var(--muted);font-size:13.5px;margin-bottom:2px}
.item:hover{background:var(--soft);color:var(--primary)}
.item.on{background:var(--primary);color:#fff;font-weight:600;box-shadow:0 6px 14px rgba(200,107,133,.28)}
.item .ic{width:16px;text-align:center;font-size:14px}
.side-foot{padding:12px 16px;border-top:1px solid var(--line);font-size:11px;color:#9AA4AC}
/* ---- 主区 ---- */
.main{flex:1;min-width:0;display:flex;flex-direction:column}
.top{height:56px;background:#fff;border-bottom:1px solid var(--line);display:flex;align-items:center;gap:16px;padding:0 24px;position:sticky;top:0;z-index:5}
.crumb{font-size:13px;color:var(--muted)}
.crumb b{color:var(--ink)}
.top-right{margin-left:auto;display:flex;align-items:center;gap:16px;font-size:13px}
.avatar{width:30px;height:30px;border-radius:50%;background:linear-gradient(135deg,var(--primary-2),var(--primary));color:#fff;display:flex;align-items:center;justify-content:center;font-size:12px}
.content{padding:24px;flex:1}
.page-head{display:flex;align-items:flex-end;gap:14px;margin-bottom:18px}
.page-head h1{font-size:20px;font-weight:700;letter-spacing:.2px}
.page-head p{font-size:12.5px;color:var(--muted);margin-bottom:2px}
.page-head .acts{margin-left:auto;display:flex;gap:8px}
/* ---- 卡片 ---- */
.card{background:var(--card);border:1px solid var(--line);border-radius:var(--radius);padding:18px;box-shadow:var(--shadow)}
.card h3{font-size:14px;font-weight:600;margin-bottom:14px;display:flex;align-items:center;gap:8px}
.card h3 .sub{font-size:11.5px;color:var(--muted);font-weight:400;margin-left:auto}
.grid{display:grid;gap:16px}
.g2{grid-template-columns:repeat(2,minmax(0,1fr))}
.g3{grid-template-columns:repeat(3,minmax(0,1fr))}
.g4{grid-template-columns:repeat(4,minmax(0,1fr))}
.g5{grid-template-columns:repeat(5,minmax(0,1fr))}
.kpi{background:#fff;border:1px solid var(--line);border-radius:12px;padding:14px 16px;box-shadow:var(--shadow)}
.kpi .lb{font-size:12px;color:var(--muted);display:flex;align-items:center;gap:6px}
.kpi .vv{font-size:24px;font-weight:700;margin-top:6px;letter-spacing:-.5px;font-variant-numeric:tabular-nums}
.kpi .vv small{font-size:12px;font-weight:500;color:var(--muted);margin-left:3px}
.kpi .df{font-size:11.5px;margin-top:4px;color:var(--ok)}
.kpi .df.down{color:var(--danger)}
.kpi.hl{background:linear-gradient(135deg,#fff,var(--soft));border-color:#EFD7DE}
/* ---- 按钮 ---- */
.btn{display:inline-flex;align-items:center;gap:6px;height:34px;padding:0 16px;border-radius:9px;border:1px solid var(--line);background:#fff;color:var(--ink);font-size:13px;cursor:pointer;font-family:inherit}
.btn:hover{border-color:var(--primary-2);color:var(--primary)}
.btn.pri{background:var(--primary);border-color:var(--primary);color:#fff;font-weight:500;box-shadow:0 6px 14px rgba(200,107,133,.22)}
.btn.pri:hover{background:#BC5C77;color:#fff}
.btn.ghost{background:transparent;border-color:var(--primary-2);color:var(--primary)}
.btn.sm{height:28px;padding:0 11px;font-size:12px;border-radius:7px}
.btn.danger{border-color:#E9C3C8;color:var(--danger)}
.btn.link{border:none;background:none;color:var(--primary);padding:0 4px;height:auto}
/* ---- 筛选 ---- */
.filters{background:#fff;border:1px solid var(--line);border-radius:12px;padding:12px 16px;display:flex;flex-wrap:wrap;gap:12px;align-items:center;margin-bottom:14px}
.filters .fi{display:flex;align-items:center;gap:8px;font-size:12.5px;color:var(--muted)}
.inp{height:32px;border:1px solid var(--line);border-radius:8px;padding:0 10px;font-size:13px;background:#fff;color:var(--ink);min-width:200px;font-family:inherit;width:200px}
.sel{height:32px;border:1px solid var(--line);border-radius:8px;padding:0 8px;font-size:13px;background:#fff;color:var(--ink);width:180px;font-family:inherit}
.spacer{flex:1}
/* ---- 表格 ---- */
table{width:100%;border-collapse:collapse;font-size:13px}
th{text-align:left;font-weight:500;color:var(--muted);font-size:12px;padding:11px 12px;background:#FAFAFA;border-bottom:1px solid var(--line);white-space:nowrap}
td{padding:12px;border-bottom:1px solid #F1EFF0;vertical-align:middle}
tbody tr:hover{background:#FCF8F9}
.num{font-variant-numeric:tabular-nums}
.t-right{text-align:right}
/* ---- 状态标签 ---- */
.tag{display:inline-flex;align-items:center;gap:4px;height:22px;padding:0 9px;border-radius:6px;font-size:12px;background:#F1F3F4;color:var(--muted);white-space:nowrap}
.tag.ok{background:#E8F2EC;color:var(--ok)}
.tag.warn{background:#FDF0E4;color:#A9651C}
.tag.dan{background:#FBEAEC;color:var(--danger)}
.tag.pri{background:var(--soft);color:var(--primary)}
.tag.acc{background:#E9F3F0;color:#2F6E60}
/* ---- 分页 ---- */
.pager{display:flex;justify-content:flex-end;align-items:center;gap:6px;padding:14px 4px 2px;font-size:12.5px;color:var(--muted)}
.pager .pg{min-width:30px;height:28px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;justify-content:center;background:#fff}
.pager .pg.on{background:var(--primary);border-color:var(--primary);color:#fff}
/* ---- 表单 ---- */
.form{max-width:560px}
.frow{margin-bottom:16px}
.frow label{display:block;font-size:12.5px;color:var(--muted);margin-bottom:6px}
.frow label i{color:var(--danger);font-style:normal;margin-right:2px}
.frow .inp,.frow .sel{width:100%}
.hint{font-size:11.5px;color:var(--muted);margin-top:5px}
.row2{display:grid;grid-template-columns:1fr 1fr;gap:14px}
/* ---- 图表（纯 CSS/SVG，无外部依赖） ---- */
.chart{height:150px;display:flex;align-items:flex-end;gap:10px;padding-top:8px}
.chart .bar{flex:1;background:linear-gradient(180deg,var(--primary-2),#F0C6D2);border-radius:6px 6px 0 0;position:relative}
.chart .bar span{position:absolute;bottom:-20px;left:0;right:0;text-align:center;font-size:10.5px;color:var(--muted)}
.donut{width:132px;height:132px;border-radius:50%;position:relative;margin:0 auto}
.donut::after{content:"";position:absolute;inset:26px;background:#fff;border-radius:50%}
.donut .ctr{position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;justify-content:center;z-index:2}
.donut .ctr b{font-size:19px}
.donut .ctr s{font-size:10.5px;color:var(--muted);text-decoration:none}
.legend{display:flex;flex-direction:column;gap:7px;font-size:12px}
.legend i{width:9px;height:9px;border-radius:3px;display:inline-block;margin-right:7px}
.funnel{display:flex;flex-direction:column;gap:6px}
.funnel .fv{height:26px;background:linear-gradient(90deg,var(--primary),var(--primary-2));border-radius:5px;display:flex;align-items:center;padding:0 10px;color:#fff;font-size:11.5px;justify-content:space-between}
.bars-h{display:flex;flex-direction:column;gap:11px}
.bars-h .r{display:flex;align-items:center;gap:10px;font-size:12px}
.bars-h .r .nm{width:64px;color:var(--muted)}
.bars-h .r .tr{flex:1;height:9px;background:#F2EFF0;border-radius:5px;overflow:hidden}
.bars-h .r .tr i{display:block;height:100%;background:var(--primary-2);border-radius:5px}
/* ---- 前台门户 ---- */
.portal{background:#FFF9FA;min-height:100vh}
.pnav{height:64px;background:#fff;border-bottom:1px solid var(--line);display:flex;align-items:center;gap:26px;padding:0 40px;position:sticky;top:0;z-index:6}
.pnav .logo{display:flex;align-items:center;gap:9px;font-weight:700;font-size:17px;color:var(--primary)}
.pnav .lk{font-size:14px;color:var(--muted)}
.pnav .lk.on{color:var(--primary);font-weight:600}
.pnav .sr{flex:1;max-width:380px;margin:0 auto;height:36px;border:1px solid var(--line);border-radius:18px;background:#FAFAFA;display:flex;align-items:center;padding:0 14px;color:#9AA4AC;font-size:13px}
.pwrap{max-width:1180px;margin:0 auto;padding:22px 24px 60px}
.babybar{background:#fff;border-radius:14px;padding:12px 18px;display:flex;align-items:center;gap:14px;box-shadow:var(--shadow);margin-bottom:18px;font-size:13px}
.babybar .b{padding:6px 12px;border-radius:9px;background:var(--soft);color:var(--primary);font-weight:600}
.babybar .b.off{background:#F4F5F6;color:var(--muted);font-weight:400}
.pcard{background:#fff;border-radius:16px;overflow:hidden;box-shadow:var(--shadow)}
.pcard .ph{height:132px;display:flex;align-items:center;justify-content:center;font-size:13px;color:#fff;letter-spacing:2px}
.pcard .pb{padding:12px 14px}
.pcard .pn{font-size:13.5px;line-height:1.45;height:40px;overflow:hidden}
.pcard .pp{display:flex;align-items:baseline;gap:8px;margin-top:8px}
.pcard .pp b{color:var(--danger);font-size:17px}
.pcard .pp s{color:#9AA4AC;font-size:12px}
.pcard .pfoot{display:flex;align-items:center;gap:6px;margin-top:10px;font-size:11.5px;color:var(--muted)}
.sold{opacity:.55}
.sec-title{display:flex;align-items:center;gap:10px;margin:26px 0 14px;font-size:16px;font-weight:700}
.sec-title small{font-size:12px;color:var(--muted);font-weight:400}
/* ---- 其它 ---- */
.tabs{display:flex;gap:6px;border-bottom:1px solid var(--line);margin-bottom:16px}
.tabs .t{padding:9px 14px;font-size:13.5px;color:var(--muted);border-bottom:2px solid transparent;cursor:pointer}
.tabs .t.on{color:var(--primary);border-color:var(--primary);font-weight:600}
.kv{display:flex;gap:12px;padding:9px 0;border-bottom:1px dashed #EFEDEE;font-size:13px}
.kv .k{width:104px;color:var(--muted);flex:0 0 104px}
.kv .v{flex:1}
.split{display:grid;grid-template-columns:340px 1fr;gap:16px;align-items:start}
.list .li{padding:12px 14px;border-radius:10px;border:1px solid transparent;cursor:pointer}
.list .li:hover{background:#FCF8F9}
.list .li.on{background:var(--soft);border-color:#EFD7DE}
.list .li .t1{font-size:13.5px;font-weight:600;display:flex;gap:8px;align-items:center}
.list .li .t2{font-size:12px;color:var(--muted);margin-top:4px}
.banner{background:linear-gradient(120deg,#F7DCE4,#EFC5D2 55%,#DCEBE6);border-radius:18px;padding:26px 30px;display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}
.banner h2{font-size:23px;letter-spacing:.5px}
.banner p{font-size:13px;color:#7A4C5B;margin-top:6px}
.mini{font-size:11.5px;color:var(--muted)}
.weight{display:flex;align-items:center;gap:12px;padding:10px 0;border-bottom:1px dashed #EFEDEE}
.weight .nm{width:132px;font-size:13px}
.weight .sl{flex:1;height:8px;background:#F2EFF0;border-radius:5px;position:relative}
.weight .sl i{position:absolute;left:0;top:0;bottom:0;background:var(--primary);border-radius:5px}
.weight .sl u{position:absolute;top:-4px;width:16px;height:16px;border-radius:50%;background:#fff;border:3px solid var(--primary);transform:translateX(-8px)}
.weight .pt{width:52px;text-align:right;font-weight:600;font-variant-numeric:tabular-nums}
"""

ICONS = """<svg xmlns="http://www.w3.org/2000/svg" style="display:none">
  <symbol id="i-dash" viewBox="0 0 24 24"><path d="M4 13h6V4H4v9zm0 7h6v-5H4v5zm10 0h6v-9h-6v9zm0-16v5h6V4h-6z"/></symbol>
  <symbol id="i-user" viewBox="0 0 24 24"><path d="M12 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10zm0 2c-5 0-9 2.5-9 5.5V22h18v-2.5c0-3-4-5.5-9-5.5z"/></symbol>
  <symbol id="i-baby" viewBox="0 0 24 24"><path d="M12 2a5 5 0 0 1 5 5c0 1.3-.5 2.4-1.3 3.3A8 8 0 1 1 7 10.3 5 5 0 0 1 12 2zm-2.5 8.5a1.2 1.2 0 1 0 0 2.4 1.2 1.2 0 0 0 0-2.4zm5 0a1.2 1.2 0 1 0 0 2.4 1.2 1.2 0 0 0 0-2.4z"/></symbol>
  <symbol id="i-goods" viewBox="0 0 24 24"><path d="M6 3h12l2 5v13H4V8l2-5zm2 2-1 3h10l-1-3H8z"/></symbol>
  <symbol id="i-coupon" viewBox="0 0 24 24"><path d="M3 7h18v4a2 2 0 0 0 0 4v4H3v-4a2 2 0 0 0 0-4V7zm6 2v8m6-8v8"/></symbol>
  <symbol id="i-order" viewBox="0 0 24 24"><path d="M6 2h9l5 5v15H6V2zm2 6h6v2H8V8zm0 5h8v2H8v-2zm0 5h5v2H8v-2z"/></symbol>
  <symbol id="i-bell" viewBox="0 0 24 24"><path d="M12 2a6 6 0 0 1 6 6v5l2 3H4l2-3V8a6 6 0 0 1 6-6zm-2 16h4a2 2 0 0 1-4 0z"/></symbol>
  <symbol id="i-report" viewBox="0 0 24 24"><path d="M4 20h16v-6H4v6zM4 12h6V4H4v8zm8 0h8V4h-8v8z"/></symbol>
  <symbol id="i-set" viewBox="0 0 24 24"><path d="M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zm9 4-2-1 1-2-2-2-2 1-1-2h-3l-1 2-2-1-2 2 1 2-2 1v3l2 1-1 2 2 2 2-1 1 2h3l1-2 2 1 2-2-1-2 2-1v-3z"/></symbol>
  <symbol id="i-risk" viewBox="0 0 24 24"><path d="M12 2 2 21h20L12 2zm0 6 5 10H7l5-10zm-1 3v4h2v-4h-2zm0 5v2h2v-2h-2z"/></symbol>
</svg>"""


def P(tpl, *args):
    """顺序替换 %s 占位符，并把 %% 还原为 %，避免 CSS 百分号与 % 格式化冲突。"""
    out = tpl
    for a in args:
        out = out.replace("%s", str(a), 1)
    return out.replace("%%", "%")

NAV = [
    ("概览", [("dashboard.html", "经营驾驶舱", "i-dash")]),
    ("会员", [("member.html", "会员档案", "i-user"), ("baby.html", "宝宝档案", "i-baby"), ("churn.html", "会员分层看板", "i-risk")]),
    ("商品", [("goods.html", "商品管理", "i-goods"), ("review.html", "评价审核", "i-order")]),
    ("营销", [("coupon.html", "优惠券配置", "i-coupon"), ("grant.html", "发券任务", "i-coupon"),
             ("repurchase.html", "复购提醒台", "i-bell"), ("report.html", "营销报表", "i-report")]),
    ("订单", [("order.html", "订单管理", "i-order")]),
    ("系统", [("sys.html", "用户与权限", "i-set"), ("churn-rule.html", "流失规则配置", "i-risk")]),
]


def admin_shell(title, active, body, crumb=None):
    nav = []
    for group, items in NAV:
        nav.append('<div class="grp">%s</div>' % group)
        for href, name, icon in items:
            cls = "item on" if href == active else "item"
            nav.append('<a class="%s" href="%s"><span class="ic"><svg width="15" height="15" fill="currentColor"><use href="#%s"/></svg></span>%s</a>'
                       % (cls, href, icon, name))
    return P("""<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8">
<title>%s · 芽芽母婴会员营销系统</title><link rel="stylesheet" href="assets/style.css"></head>
<body><div class="app">
<aside class="side">
  <div class="brand"><span class="logo">芽</span><div>芽芽母婴<small>MOM &amp; BABY CRM</small></div></div>
  <nav class="nav">%s</nav>
  <div class="side-foot">门店：杭州滨江星光店<br>v1.0 · 通道3</div>
</aside>
<div class="main">
  <header class="top">
    <div class="crumb">会员经营 / <b>%s</b></div>
    <div class="top-right">
      <span class="tag pri">店长 · 周敏</span>
      <svg width="17" height="17" fill="#5B6770"><use href="#i-bell"/></svg>
      <span class="avatar">周</span>
    </div>
  </header>
  <div class="content">%s</div>
</div></div>%s</body></html>""", title, "".join(nav), crumb or title, body, ICONS)


def portal_shell(title, active, body):
    links = [("mall.html", "首页"), ("my.html", "我的宝宝"), ("my.html", "我的优惠券"),
             ("order.html", "我的订单"), ("my.html", "积分中心")]
    lk = []
    for href, name in links:
        cls = "lk on" if href == active and name in ("首页", "我的宝宝") else "lk"
        lk.append('<a class="%s" href="%s">%s</a>' % (cls, href, name))
    return P("""<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8">
<title>%s · 芽芽母婴商城</title><link rel="stylesheet" href="assets/style.css"></head>
<body><div class="portal">
<header class="pnav">
  <div class="logo"><span style="font-size:20px">芽</span> 芽芽母婴商城</div>
  %s
  <div class="sr">搜索奶粉 / 纸尿裤 / 辅食</div>
  <span class="tag pri">金卡会员 · 王雅琴</span>
  <svg width="18" height="18" fill="#5B6770"><use href="#i-cart"/></svg>
</header>
<div class="pwrap">%s</div></div>%s</body></html>""", title, "".join(lk), body, ICONS)


def tags(s):
    m = {"已完成": "ok", "已发货": "acc", "待发货": "pri", "待支付": "warn", "已取消": "", "退货中": "warn",
         "已退款": "dan", "在售": "ok", "已售罄": "dan", "已下架": "", "待上架": "warn", "发放中": "ok",
         "已停发": "", "已过期": "dan", "待发布": "warn", "高风险": "dan", "中风险": "warn", "低风险": "ok",
         "已流失": "dan", "未评估": "", "已使用": "", "未使用": "pri", "已复购": "ok", "无响应": "",
         "咨询未购": "warn", "退订": "dan", "待发送": "warn", "已发送": "ok", "已跳过": "", "正常": "ok",
         "冻结": "dan", "注销": "", "黑卡": "pri", "金卡": "warn", "银卡": "acc", "普通会员": ""}
    return '<span class="tag %s">%s</span>' % (m.get(s, ""), s)


def pager(total=72, page=1, size=10):
    pages = max(1, (total + size - 1) // size)
    nums = "".join('<span class="pg %s">%s</span>' % ("on" if i == page else "", i) for i in range(1, min(pages, 5) + 1))
    return ('<div class="pager">共 %s 条　<span class="pg">‹</span>%s<span class="pg">›</span>　10 条/页</div>'
            % (total, nums))


# ---------------- 页面 ----------------
def page_login():
    return """<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8">
<title>登录 · 芽芽母婴会员营销系统</title><link rel="stylesheet" href="assets/style.css">
<style>
.lg{display:grid;grid-template-columns:1.05fr .95fr;min-height:100vh;background:#fff}
.lg .left{background:linear-gradient(150deg,#C86B85,#D98BA0 45%,#EFC5D2);color:#fff;padding:56px;position:relative;overflow:hidden;display:flex;flex-direction:column}
.lg .left h1{font-size:34px;line-height:1.35;letter-spacing:1px;margin-top:auto}
.lg .left p{font-size:14px;opacity:.92;margin-top:14px;max-width:400px}
.lg .left .kpis{display:flex;gap:26px;margin:32px 0 auto}
.lg .left .kpis div b{font-size:24px;display:block}
.lg .left .kpis div s{font-size:12px;opacity:.85;text-decoration:none}
.lg .left .ring{position:absolute;border-radius:50%;border:1px solid rgba(255,255,255,.28)}
.lg .right{display:flex;align-items:center;justify-content:center;padding:40px}
.lg form{width:360px}
.lg h2{font-size:24px;margin-bottom:6px}
.lg .sub{font-size:13px;color:var(--muted);margin-bottom:28px}
.lg .fld{margin-bottom:16px}
.lg .fld label{font-size:12.5px;color:var(--muted);display:block;margin-bottom:6px}
.lg input{width:100%;height:44px;border:1px solid var(--line);border-radius:10px;padding:0 14px;font-size:14px;font-family:inherit;background:#FCFCFC}
.lg input:focus{outline:none;border-color:var(--primary-2);background:#fff;box-shadow:0 0 0 3px rgba(227,160,180,.18)}
.lg .role{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin:6px 0 18px}
.lg .role span{height:34px;border:1px solid var(--line);border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:12px;color:var(--muted);cursor:pointer}
.lg .role span.on{border-color:var(--primary);color:var(--primary);background:var(--soft);font-weight:600}
.lg .go{width:100%;height:46px;border:none;border-radius:10px;background:var(--primary);color:#fff;font-size:15px;font-weight:600;cursor:pointer;margin-top:8px;box-shadow:0 10px 22px rgba(200,107,133,.3)}
.lg .tip{font-size:11.5px;color:#9AA4AC;margin-top:18px;text-align:center}
</style></head><body><div class="lg">
<div class="left">
  <div class="ring" style="width:420px;height:420px;right:-140px;top:-90px"></div>
  <div class="ring" style="width:280px;height:280px;right:-40px;bottom:-60px"></div>
  <div style="font-size:20px;font-weight:700;letter-spacing:2px">芽芽母婴 · MOM &amp; BABY</div>
  <h1>把宝宝的月龄<br>变成门店的生意</h1>
  <p>适龄商品推荐、宝宝档案、优惠券核销与会员流失预警，一套系统管住门店会员的每一次复购。</p>
  <div class="kpis">
    <div><b>1,286</b><s>在册会员</s></div>
    <div><b>68.4%</b><s>券核销率</s></div>
    <div><b>37</b><s>待召回会员</s></div>
  </div>
</div>
<div class="right">
  <form onsubmit="location.href='dashboard.html';return false">
    <h2>欢迎回来</h2>
    <div class="sub">请使用门店账号登录，或切换会员身份进入商城</div>
    <div class="fld"><label>登录账号</label><input value="admin" placeholder="手机号 / 工号"></div>
    <div class="fld"><label>登录密码</label><input type="password" value="123456"></div>
    <div style="font-size:12.5px;color:var(--muted);margin-bottom:6px">登录身份</div>
    <div class="role"><span class="on">管理员</span><span>门店导购</span><span>会员家长</span></div>
    <button class="go" type="submit">登录系统</button>
    <div class="tip">默认账号 admin / 123456　·　会员门户账号 13800002210 / 123456</div>
  </form>
</div></div></body></html>"""


def page_dashboard():
    kpis = [("今日新增会员", "18", "较昨日 +5", True), ("会员总数", "1,286", "本月 +142", True),
            ("今日订单数", "64", "较昨日 +9", True), ("今日销售额", "¥18,420", "客单价 ¥288", True),
            ("会员复购率", "42.6%", "环比 -1.2%", False), ("优惠券核销率", "68.4%", "环比 +3.1%", True),
            ("高风险流失会员", "37", "待48h内召回", False), ("积分发放/消耗", "1.8 : 1", "沉淀偏高", False)]
    ks = "".join('<div class="kpi %s"><div class="lb">%s</div><div class="vv">%s</div><div class="df %s">%s</div></div>'
                 % ("hl" if i in (0, 6) else "", n, v, "" if d else "down", t) for i, (n, v, t, d) in enumerate(kpis))
    bars = [("11-01", 62), ("11-02", 78), ("10-27", 48), ("10-28", 55), ("10-29", 70), ("10-30", 66), ("10-31", 88)]
    bchart = "".join('<div class="bar" style="height:%s%%"><span>%s</span></div>' % (h, d) for d, h in bars)
    body = P("""
<div class="page-head"><h1>经营驾驶舱</h1><p>数据截至 2026-11-02 08:20　·　口径：订单完成时间归属自然月</p>
  <div class="acts"><span class="tag">近 30 天</span><button class="btn">本月</button><button class="btn">本季度</button><button class="btn pri">导出报表</button></div></div>
<div class="grid g4" style="margin-bottom:16px">%s</div>
<div class="grid g3" style="margin-bottom:16px">
  <div class="card" style="grid-column:span 2"><h3>近 30 天会员新增与复购<span class="sub">新增 / 复购会员数</span></h3>
    <div class="chart">%s</div><div style="height:18px"></div></div>
  <div class="card"><h3>会员等级分布</h3>
    <div class="donut" style="background:conic-gradient(#C86B85 0 12%%,#E3A0B4 12%% 38%%,#7FB3A6 38%% 72%%,#D9DEE1 72%% 100%%)">
      <div class="ctr"><b>1,286</b><s>会员总数</s></div></div>
    <div class="legend" style="margin-top:14px">
      <div><i style="background:#C86B85"></i>黑卡 154 人 · 12%%</div>
      <div><i style="background:#E3A0B4"></i>金卡 334 人 · 26%%</div>
      <div><i style="background:#7FB3A6"></i>银卡 437 人 · 34%%</div>
      <div><i style="background:#D9DEE1"></i>普通 361 人 · 28%%</div>
    </div></div>
</div>
<div class="grid g3">
  <div class="card"><h3>各品类销售额</h3>
    <div class="bars-h" style="margin-top:6px">
      <div class="r"><span class="nm">奶粉</span><span class="tr"><i style="width:86%%"></i></span><span class="num">¥52.4k</span></div>
      <div class="r"><span class="nm">纸尿裤</span><span class="tr"><i style="width:74%%"></i></span><span class="num">¥45.1k</span></div>
      <div class="r"><span class="nm">辅食</span><span class="tr"><i style="width:48%%"></i></span><span class="num">¥29.3k</span></div>
      <div class="r"><span class="nm">洗护</span><span class="tr"><i style="width:32%%"></i></span><span class="num">¥19.6k</span></div>
      <div class="r"><span class="nm">玩具</span><span class="tr"><i style="width:22%%"></i></span><span class="num">¥13.4k</span></div>
      <div class="r"><span class="nm">孕妇用品</span><span class="tr"><i style="width:15%%"></i></span><span class="num">¥9.1k</span></div>
    </div></div>
  <div class="card"><h3>券领取到核销漏斗</h3>
    <div class="funnel" style="margin-top:4px">
      <div class="fv" style="width:100%%"><span>发放</span><span>2,480 张</span></div>
      <div class="fv" style="width:86%%"><span>领取</span><span>2,133 张</span></div>
      <div class="fv" style="width:64%%"><span>使用</span><span>1,459 张</span></div>
      <div class="fv" style="width:45%%"><span>带来复购</span><span>1,038 单</span></div>
    </div>
    <div class="mini" style="margin-top:14px">核销率 68.4%%，高于门店均值 61%%</div></div>
  <div class="card"><h3>流失风险分布</h3>
    <div class="donut" style="background:conic-gradient(#B02A37 0 9%%,#D9A24A 9%% 26%%,#2B7955 26%% 74%%,#D9DEE1 74%% 100%%)">
      <div class="ctr"><b>37</b><s>高风险</s></div></div>
    <div class="legend" style="margin-top:14px">
      <div><i style="background:#B02A37"></i>高风险 37 人 · 9%%</div>
      <div><i style="background:#D9A24A"></i>中风险 72 人 · 17%%</div>
      <div><i style="background:#2B7955"></i>低风险 214 人 · 48%%</div>
      <div><i style="background:#D9DEE1"></i>未评估 116 人 · 26%%</div>
    </div></div>
</div>""", ks, bchart)
    return admin_shell("经营驾驶舱", "dashboard.html", body)


def page_member():
    rows = [
        ("HY2611020001", "王雅琴", "138****2210", "金卡", "门店扫码", "3,286.50", 14, "1,286", "2026-10-28 19:20", "低风险", "正常"),
        ("HY2611020002", "李思远", "139****8832", "黑卡", "导购推荐", "9,842.00", 31, "3,140", "2026-11-01 11:05", "低风险", "正常"),
        ("HY2611020003", "陈梦琪", "137****4415", "银卡", "小程序", "1,420.80", 8, "420", "2026-09-12 15:40", "中风险", "正常"),
        ("HY2611020004", "张若彤", "136****9901", "普通会员", "小程序", "268.00", 2, "86", "2026-07-03 10:12", "高风险", "正常"),
        ("HY2611020005", "刘一诺", "135****2277", "金卡", "门店扫码", "4,102.60", 19, "980", "2026-10-30 16:22", "低风险", "正常"),
        ("HY2611020006", "赵miumiu", "134****6633", "普通会员", "门店扫码", "0.00", 0, "0", "-", "未评估", "冻结"),
        ("HY2611020007", "孙悦然", "133****1188", "银卡", "导购推荐", "1,860.40", 9, "512", "2026-08-19 09:30", "已流失", "正常"),
    ]
    trs = "".join("""<tr><td class="num">%s</td><td><b>%s</b></td><td class="num">%s</td><td>%s</td><td>%s</td>
<td class="num t-right">¥%s</td><td class="num t-right">%s</td><td class="num t-right">%s</td><td class="num">%s</td>
<td>%s</td><td>%s</td><td><a class="btn link">详情</a><a class="btn link">发券</a></td></tr>"""
               % (r[0], r[1], r[2], tags(r[3]), r[4], r[5], r[6], r[7], r[8], tags(r[9]), tags(r[10])) for r in rows)
    body = P("""
<div class="page-head"><h1>会员档案</h1><p>在册会员 1,286 人，其中 90 天未到店 37 人</p>
  <div class="acts"><button class="btn">导入会员</button><button class="btn pri">+ 新增会员</button></div></div>
<div class="filters">
  <div class="fi">等级<select class="sel"><option>全部</option><option>黑卡</option><option>金卡</option></select></div>
  <div class="fi">风险<select class="sel"><option>全部</option><option>高风险</option></select></div>
  <div class="fi">注册渠道<select class="sel"><option>全部</option><option>门店扫码</option></select></div>
  <input class="inp" placeholder="会员编号 / 手机号 / 昵称">
  <button class="btn pri">查询</button><button class="btn">重置</button><div class="spacer"></div>
  <span class="mini">共 1,286 条</span>
</div>
<div class="card" style="padding:0">
<table><thead><tr><th>会员编号</th><th>昵称</th><th>手机号</th><th>等级</th><th>注册渠道</th><th class="t-right">累计消费</th>
<th class="t-right">消费次数</th><th class="t-right">积分</th><th>最近购买</th><th>流失风险</th><th>状态</th><th>操作</th></tr></thead>
<tbody>%s</tbody></table>%s</div>""", trs, pager(1286))
    return admin_shell("会员档案", "member.html", body)


def page_baby():
    rows = [("BB000101", "王雅琴", "安宝", "女", "2025-03-18", "20 个月", "84.5", "11.2", "牛奶蛋白过敏", "3段", "XL", "有效"),
            ("BB000102", "李思远", "小满", "男", "2024-11-02", "24 个月", "89.0", "12.6", "-", "3段", "XXL", "有效"),
            ("BB000203", "陈梦琪", "团团", "男", "2026-01-09", "9 个月", "71.2", "8.8", "湿疹", "2段", "L", "有效"),
            ("BB000304", "张若彤", "糯米", "女", "2026-06-21", "4 个月", "63.5", "6.9", "-", "1段", "M", "有效"),
            ("BB000405", "刘一诺", "果果", "女", "2023-08-14", "39 个月", "98.0", "14.5", "花生过敏", "4段", "未使用", "停用")]
    trs = "".join(P("""<tr><td class="num">%s</td><td>%s</td><td><b>%s</b></td><td>%s</td><td class="num">%s</td>
<td><span class="tag pri">%s</span></td><td class="num">%s cm</td><td class="num">%s kg</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td>
<td><a class="btn link">修改</a><a class="btn link">适龄推荐</a></td></tr>""", *r) for r in rows)
    body = P("""
<div class="page-head"><h1>宝宝档案</h1><p>月龄每晚按出生日期重算，不手工录入；单会员最多 5 个宝宝</p>
  <div class="acts"><button class="btn pri">+ 建立宝宝档案</button></div></div>
<div class="split">
  <div class="card"><h3>新建档案<span class="sub">导购代录</span></h3>
    <div class="form">
      <div class="frow"><label><i>*</i>所属会员</label><input class="inp" value="王雅琴 · 138****2210"></div>
      <div class="frow"><label><i>*</i>宝宝昵称</label><input class="inp" placeholder="1 至 15 个字"></div>
      <div class="row2">
        <div class="frow"><label>性别</label><select class="sel" style="width:100%"><option>女</option><option>男</option><option>未知</option></select></div>
        <div class="frow"><label><i>*</i>出生日期</label><input class="inp" value="2025-03-18"></div>
      </div>
      <div class="frow"><label>月龄（自动计算）</label><input class="inp" value="20 个月" disabled style="background:#F7F7F8">
        <div class="hint">选择出生日期后实时计算；跨月最后一天按整月计，不多算一个月</div></div>
      <div class="row2">
        <div class="frow"><label>身高 cm</label><input class="inp" value="84.5"></div>
        <div class="frow"><label>体重 kg</label><input class="inp" value="11.2"></div>
      </div>
      <div class="row2">
        <div class="frow"><label>当前奶粉段位</label><select class="sel" style="width:100%"><option>3段</option><option>1段</option><option>2段</option><option>4段</option></select></div>
        <div class="frow"><label>纸尿裤尺码</label><select class="sel" style="width:100%"><option>XL</option><option>L</option><option>XXL</option></select></div>
      </div>
      <div class="frow"><label>过敏史</label><textarea class="inp" style="height:64px;width:100%;padding-top:8px">牛奶蛋白过敏</textarea></div>
      <div class="notice" style="background:#FDF0E4;color:#A9651C;padding:10px 12px;border-radius:9px;font-size:12px;margin-bottom:14px">
        修改出生日期导致月龄跨段位时，会提示当前奶粉段位与新标签不一致，需二次确认。</div>
      <button class="btn pri" style="width:100%">保存档案</button>
    </div>
  </div>
  <div class="card" style="padding:0">
    <div style="padding:16px 16px 6px"><h3 style="margin:0">宝宝档案列表<span class="sub">共 412 条</span></h3></div>
    <table><thead><tr><th>编号</th><th>会员</th><th>昵称</th><th>性别</th><th>出生日期</th><th>月龄</th><th>身高</th><th>体重</th>
    <th>过敏史</th><th>段位</th><th>纸尿裤</th><th>状态</th><th>操作</th></tr></thead><tbody>%s</tbody></table>%s</div>
</div>""", trs, pager(412))
    return admin_shell("宝宝档案", "baby.html", body)


def page_goods():
    goods = [("MY2611020001", "爱他美卓萃 3 段 800g", "爱他美", "奶粉", "12-36 月", "800g/罐", "328.00", "298.00", 46, "在售", "#C86B85"),
             ("MY2611020002", "帮宝适一级帮 L 码 42 片", "帮宝适", "纸尿裤", "9-24 月", "L码42片", "168.00", "152.00", 8, "在售", "#E3A0B4"),
             ("MY2611020003", "花王妙而舒 M 码 54 片", "花王", "纸尿裤", "4-12 月", "M码54片", "138.00", "128.00", 0, "已售罄", "#7FB3A6"),
             ("MY2611020004", "嘉宝原味米粉 250g", "嘉宝", "辅食", "6-18 月", "250g/盒", "58.00", "52.00", 72, "在售", "#C89F6B"),
             ("MY2611020005", "美德乐丝韵吸奶器单边", "美德乐", "孕妇用品", "0-0 月", "单边电动", "1,299.00", "1,199.00", 5, "在售", "#8E8AA8"),
             ("MY2611020006", "贝亲宽口径奶瓶 240ml", "贝亲", "洗护", "0-24 月", "240ml", "96.00", None, 33, "在售", "#6FA3B8")]
    trs = ""
    for g in goods:
        price_m = ('¥' + g[7]) if g[7] else '<span class="mini">未配置</span>'
        trs += P("""<tr><td class="num">%s</td>
<td><div style="display:flex;align-items:center;gap:10px"><span style="width:38px;height:30px;border-radius:6px;background:%s;opacity:.7"></span><span>%s</span></div></td>
<td>%s</td><td>%s</td><td><span class="tag acc">%s</span></td><td>%s</td><td class="num t-right">¥%s</td>
<td class="num t-right">%s</td><td class="num t-right">%s</td><td>%s</td>
<td><a class="btn link">详情</a><a class="btn link">%s</a></td></tr>""",
            g[0], g[9], g[1], g[2], g[3], g[4], g[5], g[6], price_m, g[8], tags(g[9]),
            "补货上架" if g[9] == "已售罄" else "编辑")
    body = P("""
<div class="page-head"><h1>商品管理</h1><p>共 168 个 SKU　·　会员价不得高于售价，售价调整不回溯已生成订单</p>
  <div class="acts"><button class="btn">适龄标签维护</button><button class="btn pri">+ 新增商品</button></div></div>
<div class="filters">
  <div class="fi">品类<select class="sel"><option>全部</option><option>奶粉</option><option>纸尿裤</option></select></div>
  <div class="fi">月龄<select class="sel"><option>全部</option><option>0-6 月</option><option>7-12 月</option><option>13-36 月</option></select></div>
  <div class="fi">状态<select class="sel"><option>全部</option><option>在售</option><option>已售罄</option></select></div>
  <input class="inp" placeholder="商品编号 / 名称 / 品牌">
  <button class="btn pri">查询</button><button class="btn">重置</button>
</div>
<div class="card" style="padding:0">
<table><thead><tr><th>商品编号</th><th>商品名称</th><th>品牌</th><th>品类</th><th>适用月龄</th><th>规格</th>
<th class="t-right">售价</th><th class="t-right">会员价</th><th class="t-right">库存</th><th>状态</th><th>操作</th></tr></thead>
<tbody>%s</tbody></table>%s</div>
<div class="mini" style="margin-top:12px">库存为 0 时自动置为「已售罄」并从商城前台隐藏，补货后需运营手动恢复在售。</div>""", trs, pager(168))
    return admin_shell("商品管理", "goods.html", body)


def page_coupon():
    rows = [("QY2611010001", "金卡专享满 300 减 50", "满减", "300.00", "50.00", "全场通用", "定向发放", "2026-11-01 ~ 2026-12-31", "2000", "1284", "发放中"),
            ("QY2611010002", "新人礼包满 99 减 20", "满减", "99.00", "20.00", "全场通用", "新人礼包", "2026-10-01 ~ 2026-12-31", "5000", "3120", "发放中"),
            ("QY2611010003", "纸尿裤品类满 199 减 30", "满减", "199.00", "30.00", "指定品类", "主动领取", "2026-10-15 ~ 2026-11-15", "800", "800", "已停发"),
            ("QY2611010004", "黑卡 9 折券", "折扣", "0.00", "9.00", "全场通用", "定向发放", "2026-09-01 ~ 2026-11-30", "300", "214", "发放中"),
            ("QY2611010005", "辅食立减 10 元", "立减", "0.00", "10.00", "指定品类", "积分兑换", "2026-08-01 ~ 2026-10-31", "1000", "1000", "已过期")]
    trs = "".join("""<tr><td class="num">%s</td><td><b>%s</b></td><td>%s</td><td class="num t-right">¥%s</td>
<td class="num t-right">%s</td><td>%s</td><td>%s</td><td class="num">%s</td><td class="num t-right">%s</td>
<td class="num t-right">%s</td><td>%s</td><td><a class="btn link">发券</a><a class="btn link">编辑</a></td></tr>"""
               % (r[0], r[1], r[2], r[3], ("¥" + r[4]) if r[2] != "折扣" else (r[4] + " 折"), r[5], r[6], r[7], r[8], r[9], tags(r[10])) for r in rows)
    body = P("""
<div class="page-head"><h1>优惠券配置</h1><p>券成本预算需店长审核；满减券门槛必须大于优惠金额</p>
  <div class="acts"><button class="btn">券预算审核</button><button class="btn pri">+ 新建券活动</button></div></div>
<div class="grid g4" style="margin-bottom:16px">
  <div class="kpi hl"><div class="lb">本月发放</div><div class="vv">2,480<small>张</small></div><div class="df">预算占用 62%</div></div>
  <div class="kpi"><div class="lb">已核销</div><div class="vv">1,459<small>张</small></div><div class="df">核销率 68.4%</div></div>
  <div class="kpi"><div class="lb">券带动销售额</div><div class="vv">¥86,420</div><div class="df">ROI 3.4</div></div>
  <div class="kpi"><div class="lb">券成本</div><div class="vv">¥25,180</div><div class="df down">较上月 +12%</div></div>
</div>
<div class="card" style="padding:0">
<table><thead><tr><th>券编号</th><th>券名称</th><th>类型</th><th class="t-right">门槛</th><th class="t-right">优惠</th><th>适用范围</th>
<th>发放方式</th><th>有效期</th><th class="t-right">总量</th><th class="t-right">已领</th><th>状态</th><th>操作</th></tr></thead>
<tbody>%s</tbody></table>%s</div>""", trs, pager(12, 1, 10))
    return admin_shell("优惠券配置", "coupon.html", body)


def page_grant():
    rows = [("王雅琴", "金卡", "中风险", "138****2210", "金卡专享满 300 减 50", "已发送"),
            ("陈梦琪", "银卡", "中风险", "137****4415", "纸尿裤品类满 199 减 30", "已发送"),
            ("张若彤", "普通会员", "高风险", "136****9901", "新人礼包满 99 减 20", "已发送"),
            ("孙悦然", "银卡", "已流失", "133****1188", "金卡专享满 300 减 50", "待发送"),
            ("李思远", "黑卡", "低风险", "139****8832", "黑卡 9 折券", "已跳过")]
    trs = "".join('<tr><td><b>%s</b></td><td>%s</td><td>%s</td><td class="num">%s</td><td>%s</td><td>%s</td></tr>'
                  % (r[0], tags(r[1]), tags(r[2]), r[3], r[4], tags(r[5])) for r in rows)
    body = P("""
<div class="page-head"><h1>发券任务</h1><p>按会员等级与流失风险定向发放，同一会员同一券活动最多 1 张</p>
  <div class="acts"><button class="btn">发放记录</button><button class="btn pri">发起定向发券</button></div></div>
<div class="grid g2">
  <div class="card"><h3>筛选目标会员</h3>
    <div class="form">
      <div class="frow"><label>券活动</label><select class="sel" style="width:100%"><option>金卡专享满 300 减 50（剩余 716 张）</option><option>新人礼包满 99 减 20</option></select></div>
      <div class="row2">
        <div class="frow"><label>会员等级</label><select class="sel" style="width:100%"><option>金卡 + 黑卡</option><option>仅金卡</option></select></div>
        <div class="frow"><label>流失风险</label><select class="sel" style="width:100%"><option>中风险 + 高风险</option><option>仅高风险</option></select></div>
      </div>
      <div class="frow"><label>最近购买间隔</label><select class="sel" style="width:100%"><option>30 天以上</option><option>60 天以上</option><option>90 天以上</option></select></div>
      <div style="background:var(--soft);border-radius:10px;padding:14px;margin-bottom:14px">
        <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:6px"><span>预计覆盖会员</span><b style="color:var(--primary)">216 人</b></div>
        <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:6px"><span>预计券成本</span><b>¥10,800</b></div>
        <div style="display:flex;justify-content:space-between;font-size:13px"><span>预算占用后</span><b>62% → 78%</b></div>
        <div class="hint">超出 80% 需店长审批；已领过该券的会员自动跳过，不重复占用总量。</div>
      </div>
      <button class="btn pri" style="width:100%">提交发放（需店长审核）</button>
    </div>
  </div>
  <div class="card" style="padding:0">
    <div style="padding:16px 16px 8px"><h3 style="margin:0">本次任务名单<span class="sub">216 人，展示前 5 条</span></h3></div>
    <table><thead><tr><th>会员</th><th>等级</th><th>风险</th><th>手机号</th><th>券活动</th><th>发放状态</th></tr></thead>
    <tbody>%s</tbody></table>
    <div style="padding:14px 16px;border-top:1px solid var(--line);display:flex;gap:8px">
      <button class="btn">导出名单</button><button class="btn ghost">仅发高风险</button>
      <span class="spacer"></span><span class="mini" style="align-self:center">短信通道为模拟，不真实下发</span>
    </div>
  </div>
</div>""", trs)
    return admin_shell("发券任务", "grant.html", body)


def page_order():
    rows = [("MO261102000021", "王雅琴", 3, "868.00", "50.00", "0.00", "818.00", "818", "微信", "已完成", "2026-11-01 20:12"),
            ("MO261102000022", "李思远", 1, "168.00", "0.00", "8.00", "176.00", "176", "支付宝", "待发货", "2026-11-02 09:04"),
            ("MO261102000023", "陈梦琪", 2, "276.00", "30.00", "0.00", "246.00", "246", "微信", "已发货", "2026-11-01 14:33"),
            ("MO261102000024", "张若彤", 1, "58.00", "20.00", "8.00", "46.00", "46", "储值卡", "待支付", "2026-11-02 08:51"),
            ("MO261102000025", "刘一诺", 4, "702.00", "0.00", "0.00", "702.00", "702", "微信", "退货中", "2026-10-30 19:20"),
            ("MO261102000026", "孙悦然", 2, "196.00", "0.00", "8.00", "204.00", "204", "支付宝", "已退款", "2026-10-12 11:08"),
            ("MO261102000027", "王雅琴", 1, "328.00", "0.00", "0.00", "328.00", "328", "微信", "已取消", "2026-10-08 16:40")]
    trs = "".join("""<tr><td class="num">%s</td><td><b>%s</b></td><td class="num t-right">%s</td>
<td class="num t-right">¥%s</td><td class="num t-right">-¥%s</td><td class="num t-right">¥%s</td>
<td class="num t-right"><b>¥%s</b></td><td class="num t-right">%s</td><td>%s</td><td>%s</td><td class="num">%s</td>
<td><a class="btn link">详情</a><a class="btn link">%s</a></td></tr>"""
               % (r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], tags(r[9]), r[10],
                  {"待发货": "发货", "待支付": "取消", "已发货": "确认收货", "退货中": "审核退货"}.get(r[9], "查看")) for r in rows)
    body = P("""
<div class="page-head"><h1>订单管理</h1><p>状态流转由后端 transition 服务校验，禁止前端直接改 status</p>
  <div class="acts"><button class="btn">导出订单</button><button class="btn pri">+ 代客下单</button></div></div>
<div class="tabs"><span class="t on">全部 72</span><span class="t">待支付 6</span><span class="t">待发货 11</span>
<span class="t">已发货 14</span><span class="t">已完成 32</span><span class="t">退货中 3</span><span class="t">已取消 6</span></div>
<div class="filters">
  <div class="fi">下单时间<input class="inp" value="2026-10-01 ~ 2026-11-02" style="width:230px"></div>
  <div class="fi">支付方式<select class="sel"><option>全部</option><option>微信</option><option>支付宝</option></select></div>
  <input class="inp" placeholder="订单编号 / 会员昵称">
  <button class="btn pri">查询</button><button class="btn">重置</button>
</div>
<div class="card" style="padding:0">
<table><thead><tr><th>订单编号</th><th>会员</th><th class="t-right">件数</th><th class="t-right">商品金额</th><th class="t-right">优惠</th>
<th class="t-right">运费</th><th class="t-right">实付</th><th class="t-right">积分</th><th>支付</th><th>状态</th><th>下单时间</th><th>操作</th></tr></thead>
<tbody>%s</tbody></table>%s</div>
<div class="mini" style="margin-top:12px">待支付订单 30 分钟内未支付将自动取消，并回补库存、返还优惠券；退款完成后按比例扣回积分并重算会员等级。</div>""", trs, pager(72))
    return admin_shell("订单管理", "order.html", body)


def page_repurchase():
    items = [("安宝 · 爱他美卓萃 3 段", "预计 11-05 用完 · 剩 3 天", "on"), ("小满 · 帮宝适一级帮 L 码", "预计 11-06 用完 · 剩 4 天", ""),
             ("团团 · 花王妙而舒 M 码", "预计 11-08 用完 · 剩 6 天", ""), ("糯米 · 爱他美卓萃 1 段", "预计 11-09 用完 · 剩 7 天", ""),
             ("果果 · 嘉宝原味米粉", "预计 11-12 用完 · 剩 10 天", "")]
    lis = "".join('<div class="li %s"><div class="t1">%s</div><div class="t2">%s</div></div>' % (c, t, s) for t, s, c in items)
    body = P("""
<div class="page-head"><h1>复购提醒台</h1><p>按预计用完日期前 3 天生成，同一会员同一商品 7 天内最多一条</p>
  <div class="acts"><button class="btn">批量生成提醒</button><button class="btn pri">导出跟进名单</button></div></div>
<div class="split">
  <div class="card"><h3>待跟进提醒<span class="sub">24 条</span></h3>
    <div class="filters" style="margin:0 0 10px;padding:8px 10px">
      <select class="sel" style="width:100%"><option>按预计用完日期排序</option><option>按风险等级排序</option></select></div>
    <div class="list">%s</div>
    <div class="mini" style="margin-top:12px">已退订短信的会员只允许站内消息与导购电话。</div>
  </div>
  <div class="grid" style="gap:16px">
    <div class="card"><h3>安宝 · 爱他美卓萃 3 段<span class="sub">提醒编号 FG2611020001</span></h3>
      <div class="grid g2">
        <div>
          <div class="kv"><span class="k">会员</span><span class="v">王雅琴 · 金卡 · 138****2210</span></div>
          <div class="kv"><span class="k">宝宝月龄</span><span class="v">安宝 · 20 个月 · 3段</span></div>
          <div class="kv"><span class="k">上次购买</span><span class="v">2026-10-06（800g × 1）</span></div>
          <div class="kv"><span class="k">预计用完</span><span class="v">2026-11-05　<span class="tag warn">剩 3 天</span></span></div>
          <div class="kv"><span class="k">估算依据</span><span class="v">日均 26g，800g ÷ 26g ≈ 31 天</span></div>
          <div class="kv"><span class="k">最近购买间隔</span><span class="v">27 天</span></div>
        </div>
        <div>
          <div class="kv"><span class="k">流失风险</span><span class="v"><span class="tag ok">低风险 32 分</span></span></div>
          <div class="kv"><span class="k">提醒方式</span><span class="v">站内消息 + 导购电话</span></div>
          <div class="kv"><span class="k">计划发送</span><span class="v">2026-11-02 10:00</span></div>
          <div class="kv"><span class="k">发送状态</span><span class="v"><span class="tag warn">待发送</span></span></div>
          <div class="kv"><span class="k">负责人</span><span class="v">导购 林小满</span></div>
          <div class="kv"><span class="k">适龄推荐</span><span class="v">爱他美卓萃 3 段 / 嘉宝米粉</span></div>
        </div>
      </div>
    </div>
    <div class="card"><h3>登记跟进结果<span class="sub">48 小时内必须跟进</span></h3>
      <div class="grid g3">
        <div class="frow"><label>会员响应</label><select class="sel" style="width:100%"><option>已复购</option><option>咨询未购</option><option>无响应</option><option>退订</option></select></div>
        <div class="frow"><label>实际购买商品</label><input class="inp" style="width:100%" value="爱他美卓萃 3 段 800g × 2"></div>
        <div class="frow"><label>跟进说明</label><input class="inp" style="width:100%" placeholder="不少于 5 个字"></div>
      </div>
      <div style="display:flex;gap:8px"><button class="btn pri">提交结果</button><button class="btn">标记为已跳过</button>
        <span class="spacer"></span><span class="mini" style="align-self:center">已复购的会员不再重复提醒</span></div>
    </div>
  </div>
</div>""", lis)
    return admin_shell("复购提醒台", "repurchase.html", body)


def page_churn():
    levels = [("黑卡", 154, "12%", "#C86B85", "¥8,000 以上 / 20 次以上"), ("金卡", 334, "26%", "#E3A0B4", "¥3,000 以上 / 10 次以上"),
              ("银卡", 437, "34%", "#7FB3A6", "¥1,000 以上 / 5 次以上"), ("普通会员", 361, "28%", "#D9DEE1", "其他")]
    cards = "".join(P("""<div class="kpi"><div class="lb"><i style="width:8px;height:8px;border-radius:3px;background:%s;display:inline-block"></i>%s</div>
<div class="vv">%s<small>人 · %s</small></div><div class="df" style="color:var(--muted)">%s</div></div>""", c[3], c[0], c[1], c[2], c[4]) for c in levels)
    rows = [("HY2611020004", "张若彤", "普通会员", "268.00", 1, "96 天", "88", "高风险", "待召回", "王璐"),
            ("HY2611020007", "孙悦然", "银卡", "1,860.40", 9, "74 天", "81", "高风险", "已召回", "林小满"),
            ("HY2611020011", "周子墨", "银卡", "2,140.00", 8, "68 天", "76", "高风险", "待召回", "王璐"),
            ("HY2611020003", "陈梦琪", "银卡", "1,420.80", 8, "51 天", "62", "中风险", "跟进中", "林小满"),
            ("HY2611020018", "何欣妍", "金卡", "3,860.00", 16, "45 天", "57", "中风险", "跟进中", "王璐")]
    trs = "".join("""<tr><td class="num">%s</td><td><b>%s</b></td><td>%s</td><td class="num t-right">¥%s</td>
<td class="num t-right">%s</td><td class="num">%s</td><td class="num"><b style="color:%s">%s</b></td><td>%s</td>
<td>%s</td><td>%s</td><td><a class="btn link">召回详情</a><a class="btn link">人工修正</a></td></tr>"""
               % (r[0], r[1], tags(r[2]), r[3], r[4], r[5], "var(--danger)" if int(r[6]) >= 75 else "var(--muted)", r[6],
                  tags(r[7]), tags(r[8]), r[9]) for r in rows)
    body = P("""
<div class="page-head"><h1>会员分层看板</h1><p>每晚 02:00 重算流失风险分与等级　·　规则版本 v3（2026-10-20 生效）</p>
  <div class="acts"><button class="btn">重算全部</button><button class="btn pri">分配到导购</button></div></div>
<div class="grid g4" style="margin-bottom:16px">%s</div>
<div class="grid g3" style="margin-bottom:16px">
  <div class="card"><h3>流失风险分布<span class="sub">1,286 人</span></h3>
    <div class="donut" style="background:conic-gradient(#B02A37 0 9%%,#D9A24A 9%% 26%%,#2B7955 26%% 74%%,#D9DEE1 74%% 100%%)">
      <div class="ctr"><b>9%%</b><s>高风险占比</s></div></div>
    <div class="legend" style="margin-top:14px">
      <div><i style="background:#B02A37"></i>高风险 37 人</div><div><i style="background:#D9A24A"></i>中风险 72 人</div>
      <div><i style="background:#2B7955"></i>低风险 214 人</div><div><i style="background:#D9DEE1"></i>未评估 963 人</div></div></div>
  <div class="card"><h3>召回效果<span class="sub">近 90 天</span></h3>
    <div class="bars-h" style="margin-top:8px">
      <div class="r"><span class="nm">已分配</span><span class="tr"><i style="width:100%%"></i></span><span class="num">37</span></div>
      <div class="r"><span class="nm">已联系</span><span class="tr"><i style="width:84%%"></i></span><span class="num">31</span></div>
      <div class="r"><span class="nm">已复购</span><span class="tr"><i style="width:46%%"></i></span><span class="num">17</span></div>
      <div class="r"><span class="nm">召回成功率</span><span class="tr"><i style="width:46%%"></i></span><span class="num">54.8%</span></div>
    </div>
    <div class="mini" style="margin-top:14px">规则采纳率 78.4%　·　人工修正率 12.6%</div></div>
  <div class="card"><h3>待召回优先级说明</h3>
    <div class="kv"><span class="k">高风险 ≥75</span><span class="v">48 小时内导购电话召回 + 定向满减券</span></div>
    <div class="kv"><span class="k">中风险 50-74</span><span class="v">7 天内推送复购提醒与适龄商品</span></div>
    <div class="kv"><span class="k">低风险 &lt;50</span><span class="v">按月推送复购提醒，不额外发券</span></div>
    <div class="kv"><span class="k">人工修正</span><span class="v">可修正一次，原因不少于 5 字，原值保留</span></div>
    <div class="mini" style="margin-top:10px">分值越高流失风险越大；距今天数按 90 天分段线性归一。</div></div>
</div>
<div class="card" style="padding:0">
  <div style="padding:16px 16px 8px"><h3 style="margin:0">待召回名单<span class="sub">按风险分降序，前 5 条</span></h3></div>
  <table><thead><tr><th>会员编号</th><th>昵称</th><th>等级</th><th class="t-right">累计消费</th><th class="t-right">消费次数</th>
  <th>距上次购买</th><th class="t-right">风险分</th><th>风险等级</th><th>召回状态</th><th>负责人</th><th>操作</th></tr></thead>
  <tbody>%s</tbody></table>%s</div>""", cards, trs, pager(37))
    return admin_shell("会员分层看板", "churn.html", body)


def page_churn_rule():
    ws = [("最近购买间隔", 35, "member.last_buy_time 距今天数，超 90 天记 100 分"),
          ("近 90 天消费频次", 25, "已完成订单数按月折算，注册不足 90 天按实际天数"),
          ("客单价变化率", 20, "近 90 天对比上一个 90 天，无上期样本按 0 分"),
          ("优惠券使用率", 10, "已使用 / 领取，近 180 天未领券按 0 分"),
          ("宝宝月龄阶段跨度", 10, "月龄与最近购买品类适龄段差值，无档案按 60 分基准")]
    rows = "".join(P("""<div class="weight"><span class="nm">%s</span><span class="sl"><i style="width:%s%%"></i><u style="left:%s%%"></u></span>
<span class="pt">%s%%</span><span class="mini" style="width:300px">%s</span></div>""", n, w, w, w, d) for n, w, d in ws)
    body = P("""
<div class="page-head"><h1>流失规则配置</h1><p>权重合计必须 100%　·　修改仅影响新计算或主动重算的数据</p>
  <div class="acts"><button class="btn">查看历史版本</button><button class="btn pri">保存并发布 v4</button></div></div>
<div class="grid g2">
  <div class="card"><h3>因素权重<span class="sub">当前版本 v3 · 2026-10-20 生效</span></h3>
    %s
    <div style="display:flex;justify-content:space-between;padding-top:14px;font-size:13px">
      <span>权重合计</span><b style="color:var(--ok)">100%%　校验通过</b></div>
    <div class="hint" style="margin-top:8px">发布后记录版本号与生效时间，历史计算结果不受影响；重算需人工触发并留痕。</div>
  </div>
  <div class="grid" style="gap:16px">
    <div class="card"><h3>分级阈值</h3>
      <div class="kv"><span class="k">高风险</span><span class="v">风险分 ≥ 75，48 小时内电话召回 + 定向满减券</span></div>
      <div class="kv"><span class="k">中风险</span><span class="v">50 ≤ 风险分 &lt; 75，7 天内复购提醒</span></div>
      <div class="kv"><span class="k">低风险</span><span class="v">风险分 &lt; 50，按月提醒不额外发券</span></div>
      <div class="kv"><span class="k">线性归一</span><span class="v">距今天数按 90 天分段归一，超过 90 天记 100</span></div>
    </div>
    <div class="card"><h3>计算留痕示例<span class="sub">快照 #SC2611020017</span></h3>
      <div class="kv"><span class="k">会员</span><span class="v">张若彤 · 普通会员 · 规则版本 v3</span></div>
      <div class="kv"><span class="k">输入快照</span><span class="v">间隔 96 天 / 频次 0.3 次月 / 客单变化 -46% / 券使用率 0% / 无宝宝档案</span></div>
      <div class="kv"><span class="k">各因素得分</span><span class="v">100 × 35% + 82 × 25% + 74 × 20% + 100 × 10% + 60 × 10% = 88.3</span></div>
      <div class="kv"><span class="k">结论</span><span class="v"><span class="tag dan">高风险 88</span>　建议 48 小时内电话召回，并发放满 199 减 30 券</span></div>
      <div class="kv"><span class="k">未满足条件</span><span class="v">缺少宝宝档案，月龄因素按基准 60 分计入，建议导购补录</span></div>
    </div>
  </div>
</div>""", rows)
    return admin_shell("流失规则配置", "churn-rule.html", body)


def page_report():
    rows = [("金卡专享满 300 减 50", "满减", "1,284", "986", "76.8%", "¥62,180", "¥49,300", "3.2"),
            ("新人礼包满 99 减 20", "满减", "3,120", "1,842", "59.0%", "¥41,260", "¥36,840", "2.8"),
            ("纸尿裤品类满 199 减 30", "满减", "800", "612", "76.5%", "¥28,940", "¥18,360", "3.6"),
            ("黑卡 9 折券", "折扣", "214", "186", "86.9%", "¥36,420", "¥3,642", "2.4"),
            ("辅食立减 10 元", "立减", "1,000", "402", "40.2%", "¥9,860", "¥4,020", "1.9")]
    trs = "".join(P("""<tr><td><b>%s</b></td><td>%s</td><td class="num t-right">%s</td><td class="num t-right">%s</td>
<td class="num t-right"><b>%s</b></td><td class="num t-right">%s</td><td class="num t-right">%s</td><td class="num t-right">%s</td></tr>""", *r) for r in rows)
    body = P("""
<div class="page-head"><h1>营销报表</h1><p>口径按订单完成时间归属自然月，已退款订单从销售额与积分中剔除</p>
  <div class="acts"><select class="sel"><option>2026 年 11 月</option><option>2026 年 10 月</option></select>
  <button class="btn">按门店下钻</button><button class="btn pri">导出 Excel</button></div></div>
<div class="grid g5" style="margin-bottom:16px">
  <div class="kpi"><div class="lb">券领取量</div><div class="vv">5,418</div><div class="df">环比 +8.2%</div></div>
  <div class="kpi"><div class="lb">券核销量</div><div class="vv">4,028</div><div class="df">核销率 74.3%</div></div>
  <div class="kpi"><div class="lb">券带动销售额</div><div class="vv">¥178,660</div><div class="df">ROI 2.9</div></div>
  <div class="kpi"><div class="lb">会员复购率</div><div class="vv">42.6%</div><div class="df down">环比 -1.2%</div></div>
  <div class="kpi"><div class="lb">积分沉淀比</div><div class="vv">1.8<small>: 1</small></div><div class="df down">超阈值 1.5</div></div>
</div>
<div class="grid g3" style="margin-bottom:16px">
  <div class="card" style="grid-column:span 2"><h3>券活动效果<span class="sub">按券类型分组</span></h3>
    <table><thead><tr><th>券活动</th><th>类型</th><th class="t-right">领取</th><th class="t-right">核销</th><th class="t-right">核销率</th>
    <th class="t-right">带动销售额</th><th class="t-right">券成本</th><th class="t-right">ROI</th></tr></thead><tbody>%s</tbody></table></div>
  <div class="card"><h3>积分健康度</h3>
    <div class="bars-h" style="margin-top:10px">
      <div class="r"><span class="nm">发放</span><span class="tr"><i style="width:100%%"></i></span><span class="num">128.4k</span></div>
      <div class="r"><span class="nm">消耗</span><span class="tr"><i style="width:56%%"></i></span><span class="num">71.2k</span></div>
      <div class="r"><span class="nm">过期</span><span class="tr"><i style="width:9%%"></i></span><span class="num">11.6k</span></div>
      <div class="r"><span class="nm">沉淀</span><span class="tr"><i style="width:45%%"></i></span><span class="num">45.6k</span></div>
    </div>
    <div style="background:#FDF0E4;color:#A9651C;border-radius:10px;padding:11px 12px;font-size:12px;margin-top:16px">
      沉淀比 1.8 已超阈值 1.5，建议上线「积分兑纸尿裤试用装」活动消化沉淀。</div>
    <div class="kv" style="margin-top:10px"><span class="k">召回成功率</span><span class="v">54.8%（17 / 31）</span></div>
  </div>
</div>
<div class="grid g2">
  <div class="card"><h3>宝宝月龄段购买分布</h3>
    <div class="chart">
      <div class="bar" style="height:42%%"><span>0-6月</span></div><div class="bar" style="height:66%%"><span>7-12月</span></div>
      <div class="bar" style="height:92%%"><span>13-24月</span></div><div class="bar" style="height:74%%"><span>25-36月</span></div>
      <div class="bar" style="height:38%%"><span>36月+</span></div>
    </div><div style="height:18px"></div>
    <div class="mini">13-24 月龄段贡献 34% 销售额，与 3 段奶粉、L/XL 码纸尿裤重合。</div></div>
  <div class="card"><h3>按导购下钻</h3>
    <table><thead><tr><th>导购</th><th class="t-right">会员数</th><th class="t-right">本月销售额</th><th class="t-right">复购率</th><th class="t-right">召回成功</th></tr></thead>
    <tbody>
      <tr><td>林小满</td><td class="num t-right">186</td><td class="num t-right">¥42,180</td><td class="num t-right">48.2%</td><td class="num t-right">7</td></tr>
      <tr><td>王璐</td><td class="num t-right">162</td><td class="num t-right">¥36,420</td><td class="num t-right">45.6%</td><td class="num t-right">6</td></tr>
      <tr><td>陈曦</td><td class="num t-right">144</td><td class="num t-right">¥29,860</td><td class="num t-right">39.1%</td><td class="num t-right">4</td></tr>
    </tbody></table></div>
</div>""", trs)
    return admin_shell("营销报表", "report.html", body)


def page_review():
    rows = [("爱他美卓萃 3 段 800g", "王雅琴", 5, "宝宝喝了两罐，便便正常，就是价格有点小贵。", "2026-11-01 21:02", "待审核"),
            ("帮宝适一级帮 L 码 42 片", "陈梦琪", 4, "夜用不漏，但腰围偏紧，建议买大一码。", "2026-10-31 19:20", "待审核"),
            ("嘉宝原味米粉 250g", "张若彤", 2, "包装破损，客服已换货，米粉本身还行。", "2026-10-29 10:08", "已驳回")]
    trs = "".join("""<tr><td><b>%s</b></td><td>%s</td><td>%s</td><td style="max-width:420px">%s</td>
<td class="num">%s</td><td>%s</td><td><a class="btn link">通过</a><a class="btn link">驳回</a></td></tr>"""
               % (r[0], r[1], '<span style="color:#D9A24A">' + "★" * r[2] + "</span>", r[3], r[4], tags(r[5])) for r in rows)
    body = P("""
<div class="page-head"><h1>评价审核</h1><p>仅审核文字与图片合规性，差评不得删除，只做隐藏敏感内容</p>
  <div class="acts"><button class="btn">批量通过</button></div></div>
<div class="filters"><div class="fi">状态<select class="sel"><option>待审核 12</option><option>已通过</option></select></div>
<input class="inp" placeholder="商品名称 / 会员昵称"><button class="btn pri">查询</button></div>
<div class="card" style="padding:0">
<table><thead><tr><th>商品</th><th>会员</th><th>评分</th><th>评价内容</th><th>评价时间</th><th>状态</th><th>操作</th></tr></thead>
<tbody>%s</tbody></table>%s</div>""", trs, pager(42))
    return admin_shell("评价审核", "review.html", body)


def page_sys():
    body = P("""
<div class="page-head"><h1>用户与权限</h1><p>菜单权限 + 按钮权限 + 数据范围三层控制，写接口后端复核</p>
  <div class="acts"><button class="btn pri">+ 新增用户</button></div></div>
<div class="tabs"><span class="t on">用户管理</span><span class="t">角色管理</span><span class="t">菜单管理</span><span class="t">字典管理</span></div>
<div class="grid g4" style="margin-bottom:16px">
  <div class="kpi"><div class="lb">系统用户</div><div class="vv">14</div><div class="df">6 个角色</div></div>
  <div class="kpi"><div class="lb">在线会话</div><div class="vv">5</div><div class="df">近 1 小时</div></div>
  <div class="kpi"><div class="lb">今日操作日志</div><div class="vv">218</div><div class="df">写操作 64 条</div></div>
  <div class="kpi"><div class="lb">停用账号</div><div class="vv">1</div><div class="df down">需复核</div></div>
</div>
<div class="card" style="padding:0">
<table><thead><tr><th>账号</th><th>姓名</th><th>角色</th><th>数据范围</th><th>手机号</th><th>最近登录</th><th>状态</th><th>操作</th></tr></thead>
<tbody>
<tr><td><b>admin</b></td><td>系统管理员</td><td><span class="tag pri">系统管理员</span></td><td>全部</td><td class="num">138****0001</td><td class="num">2026-11-02 08:06</td><td><span class="tag ok">正常</span></td><td><a class="btn link">编辑</a><a class="btn link">分配角色</a></td></tr>
<tr><td><b>manager01</b></td><td>周敏</td><td><span class="tag pri">门店店长</span></td><td>本部门</td><td class="num">138****0012</td><td class="num">2026-11-02 08:20</td><td><span class="tag ok">正常</span></td><td><a class="btn link">编辑</a><a class="btn link">分配角色</a></td></tr>
<tr><td><b>operator01</b></td><td>许静</td><td><span class="tag acc">商城运营</span></td><td>全部</td><td class="num">138****0023</td><td class="num">2026-11-01 18:42</td><td><span class="tag ok">正常</span></td><td><a class="btn link">编辑</a><a class="btn link">分配角色</a></td></tr>
<tr><td><b>marketer01</b></td><td>顾晓</td><td><span class="tag warn">营销专员</span></td><td>全部</td><td class="num">138****0034</td><td class="num">2026-11-02 09:12</td><td><span class="tag ok">正常</span></td><td><a class="btn link">编辑</a><a class="btn link">分配角色</a></td></tr>
<tr><td><b>guide01</b></td><td>林小满</td><td><span class="tag">门店导购</span></td><td>本人</td><td class="num">138****0045</td><td class="num">2026-11-02 08:58</td><td><span class="tag ok">正常</span></td><td><a class="btn link">编辑</a><a class="btn link">分配角色</a></td></tr>
<tr><td><b>member01</b></td><td>王雅琴</td><td><span class="tag">会员家长</span></td><td>本人</td><td class="num">138****2210</td><td class="num">2026-11-01 20:10</td><td><span class="tag ok">正常</span></td><td><a class="btn link">编辑</a><a class="btn link">分配角色</a></td></tr>
</tbody></table>%s</div>""", pager(14))
    return admin_shell("用户与权限", "sys.html", body)


def page_mall():
    def card(name, brand, price, mprice, age, color, sold=False):
        return P("""<div class="pcard %s"><div class="ph" style="background:linear-gradient(135deg,%s,#F6E4E9)">%s</div>
<div class="pb"><div class="pn">%s</div><div class="pp"><b>¥%s</b><s>¥%s</s></div>
<div class="pfoot"><span class="tag acc">%s</span><span>%s</span><span style="margin-left:auto">%s</span></div></div></div>""",
            "sold" if sold else "", color, brand, name, mprice, price, age, "会员价" if not sold else "已售罄",
            "加购" if not sold else "到货提醒")
    cards1 = "".join([card("爱他美卓萃 3 段 800g", "爱他美", "328.00", "298.00", "12-36 月", "#C86B85"),
                      card("惠氏启赋蓝钻 2 段 900g", "惠氏", "368.00", "338.00", "6-12 月", "#B98BA0"),
                      card("美赞臣蓝臻 1 段 800g", "美赞臣", "398.00", "368.00", "0-6 月", "#A87C8E"),
                      card("帮宝适一级帮 L 码 42 片", "帮宝适", "168.00", "152.00", "9-24 月", "#E3A0B4")])
    cards2 = "".join([card("花王妙而舒 M 码 54 片", "花王", "138.00", "128.00", "4-12 月", "#7FB3A6", True),
                      card("尤妮佳 moony 纸尿裤 S 码", "尤妮佳", "128.00", "118.00", "0-6 月", "#8FBFAE"),
                      card("嘉宝原味米粉 250g", "嘉宝", "58.00", "52.00", "6-18 月", "#C89F6B"),
                      card("亨氏果泥 苹果味 120g", "亨氏", "19.90", "17.90", "6-24 月", "#C9A46E")])
    body = P("""
<div class="babybar"><span class="mini">当前宝宝</span><span class="b">安宝 · 20 个月 · 3段 · XL</span>
  <span class="b off">小满 · 24 个月</span><span class="b off">+ 添加宝宝</span>
  <span class="spacer"></span><span class="mini">已按 13-24 月龄筛选适龄商品</span></div>
<div class="banner"><div><h2>秋冬换季，纸尿裤囤货季</h2><p>金卡会员满 300 减 50，叠加券后 L 码低至 122 元 / 包</p>
  <div style="display:flex;gap:10px;margin-top:14px"><button class="btn pri">立即选购</button><button class="btn">领取优惠券</button></div></div>
  <div style="width:96px;height:96px;border-radius:50%;border:10px solid rgba(255,255,255,.55);position:relative"><span style="position:absolute;left:38px;top:-16px;width:20px;height:20px;border-radius:4px;background:rgba(255,255,255,.55)"></span></div></div>
<div class="sec-title">奶粉 · 按段位推荐 <small>宝宝当前 3 段</small></div>
<div class="grid g4">%s</div>
<div class="sec-title">纸尿裤 · 按体重选码 <small>当前体重 11.2kg，推荐 L / XL</small></div>
<div class="grid g4">%s</div>
<div class="sec-title">辅食与洗护 <small>6-24 月龄适龄</small></div>
<div class="grid g4">
  <div class="pcard"><div class="ph" style="background:linear-gradient(135deg,#C89F6B,#F3E4CE)">嘉宝</div><div class="pb">
    <div class="pn">嘉宝原味米粉 250g</div><div class="pp"><b>¥52.00</b><s>¥58.00</s></div>
    <div class="pfoot"><span class="tag acc">6-18 月</span><span>会员价</span><span style="margin-left:auto">加购</span></div></div></div>
  <div class="pcard"><div class="ph" style="background:linear-gradient(135deg,#6FA3B8,#DCEBF0)">贝亲</div><div class="pb">
    <div class="pn">贝亲宽口径奶瓶 240ml</div><div class="pp"><b>¥96.00</b><s>¥106.00</s></div>
    <div class="pfoot"><span class="tag acc">0-24 月</span><span>会员价</span><span style="margin-left:auto">加购</span></div></div></div>
  <div class="pcard"><div class="ph" style="background:linear-gradient(135deg,#8E8AA8,#E7E5EF)">美德乐</div><div class="pb">
    <div class="pn">美德乐丝韵吸奶器单边</div><div class="pp"><b>¥1,199.00</b><s>¥1,299.00</s></div>
    <div class="pfoot"><span class="tag acc">孕妇用品</span><span>会员价</span><span style="margin-left:auto">加购</span></div></div></div>
  <div class="pcard"><div class="ph" style="background:linear-gradient(135deg,#C86B85,#F6E4E9)">全棉时代</div><div class="pb">
    <div class="pn">全棉时代婴儿棉柔巾 100 抽</div><div class="pp"><b>¥39.90</b><s>¥45.00</s></div>
    <div class="pfoot"><span class="tag acc">0-36 月</span><span>会员价</span><span style="margin-left:auto">加购</span></div></div></div>
</div>""", cards1, cards2)
    return portal_shell("商城首页", "mall.html", body)


def page_checkout():
    rows = [("爱他美卓萃 3 段 800g", "800g/罐", "¥298.00", 1, "¥298.00"),
            ("帮宝适一级帮 L 码 42 片", "L码42片", "¥152.00", 2, "¥304.00"),
            ("嘉宝原味米粉 250g", "250g/盒", "¥52.00", 1, "¥52.00")]
    trs = "".join('<tr><td><b>%s</b><div class="mini">%s</div></td><td class="num t-right">%s</td>'
                  '<td class="num t-right">×%s</td><td class="num t-right">%s</td></tr>' % r for r in rows)
    body = P("""
<div class="sec-title">确认订单</div>
<div class="grid" style="grid-template-columns:1fr 340px;gap:18px;align-items:start">
  <div class="grid" style="gap:16px">
    <div class="card"><h3>收货信息</h3>
      <div class="kv"><span class="k">收货人</span><span class="v">王雅琴　138****2210</span></div>
      <div class="kv"><span class="k">收货地址</span><span class="v">杭州市滨江区星光大道 118 号 3 幢 1201 室</span></div>
      <div class="kv"><span class="k">发货说明</span><span class="v">门店自提可当日达；发货后不可修改收货地址</span></div>
    </div>
    <div class="card" style="padding:0"><div style="padding:16px 16px 8px"><h3 style="margin:0">商品清单</h3></div>
      <table><thead><tr><th>商品</th><th class="t-right">单价</th><th class="t-right">数量</th><th class="t-right">小计</th></tr></thead>
      <tbody>%s</tbody></table></div>
    <div class="card"><h3>优惠券<span class="sub">一单限用一张，不叠加</span></h3>
      <div class="grid g2">
        <div style="border:1px solid var(--primary);border-radius:10px;padding:12px;background:var(--soft)">
          <div style="display:flex;justify-content:space-between"><b>满 300 减 50</b><span class="tag pri">已选</span></div>
          <div class="mini" style="margin-top:6px">金卡专享 · 有效期至 2026-12-31 · 全场通用</div></div>
        <div style="border:1px solid var(--line);border-radius:10px;padding:12px;opacity:.55">
          <div style="display:flex;justify-content:space-between"><b>纸尿裤满 199 减 30</b><span class="tag">不可用</span></div>
          <div class="mini" style="margin-top:6px">与已选券互斥，不可叠加使用</div></div>
      </div>
    </div>
    <div class="card"><h3>积分抵扣<span class="sub">当前 1,286 分</span></h3>
      <div style="display:flex;align-items:center;gap:10px;font-size:13px">
        <input type="checkbox" checked> 使用 1,000 积分抵扣 ¥10.00
        <span class="mini">实付金额按整数发放积分，退款后按比例扣回</span></div>
    </div>
  </div>
  <div class="card" style="position:sticky;top:80px">
    <h3>金额明细</h3>
    <div class="kv"><span class="k">商品金额</span><span class="v t-right">¥654.00</span></div>
    <div class="kv"><span class="k">优惠券</span><span class="v t-right" style="color:var(--danger)">-¥50.00</span></div>
    <div class="kv"><span class="k">积分抵扣</span><span class="v t-right" style="color:var(--danger)">-¥10.00</span></div>
    <div class="kv"><span class="k">运费</span><span class="v t-right">¥0.00（满 199 免运费）</span></div>
    <div style="display:flex;justify-content:space-between;align-items:baseline;margin:16px 0 4px">
      <span>实付金额</span><b style="font-size:26px;color:var(--danger)">¥594.00</b></div>
    <div class="mini" style="margin-bottom:14px">预计获得 594 积分</div>
    <button class="btn pri" style="width:100%;height:44px">提交订单</button>
    <div class="mini" style="margin-top:10px;text-align:center">仅登记支付方式，不接真实支付通道</div>
  </div>
</div>""", trs)
    return portal_shell("结算", "checkout.html", body)


def page_my():
    body = """
<div class="grid" style="grid-template-columns:1fr 320px;gap:18px;align-items:start">
  <div class="grid" style="gap:16px">
    <div class="card"><h3>我的宝宝档案<span class="sub">最多 5 个</span></h3>
      <div class="grid g2">
        <div style="border:1px solid var(--line);border-radius:12px;padding:14px">
          <div style="display:flex;justify-content:space-between"><b>安宝</b><span class="tag pri">20 个月</span></div>
          <div class="kv"><span class="k">性别 / 生日</span><span class="v">女 · 2025-03-18</span></div>
          <div class="kv"><span class="k">身高 / 体重</span><span class="v">84.5 cm · 11.2 kg</span></div>
          <div class="kv"><span class="k">奶粉 / 纸尿裤</span><span class="v">3 段 · XL</span></div>
          <div class="kv"><span class="k">过敏史</span><span class="v">牛奶蛋白过敏</span></div>
        </div>
        <div style="border:1px dashed var(--primary-2);border-radius:12px;padding:14px;display:flex;align-items:center;justify-content:center;color:var(--primary);font-size:13px">
          + 添加宝宝档案</div>
      </div>
    </div>
    <div class="card"><h3>我的优惠券<span class="sub">3 张可用</span></h3>
      <div class="grid g3">
        <div style="background:linear-gradient(135deg,#C86B85,#E3A0B4);color:#fff;border-radius:12px;padding:14px">
          <div style="font-size:22px;font-weight:700">¥50</div><div style="font-size:12px;opacity:.95">满 300 可用 · 全场通用</div>
          <div style="font-size:11px;opacity:.85;margin-top:8px">有效期至 2026-12-31</div></div>
        <div style="background:linear-gradient(135deg,#7FB3A6,#A8CDC2);color:#fff;border-radius:12px;padding:14px">
          <div style="font-size:22px;font-weight:700">¥30</div><div style="font-size:12px;opacity:.95">满 199 可用 · 纸尿裤</div>
          <div style="font-size:11px;opacity:.85;margin-top:8px">有效期至 2026-11-15</div></div>
        <div style="background:linear-gradient(135deg,#C89F6B,#E2C8A4);color:#fff;border-radius:12px;padding:14px">
          <div style="font-size:22px;font-weight:700">9 折</div><div style="font-size:12px;opacity:.95">全场通用 · 最高减 100</div>
          <div style="font-size:11px;opacity:.85;margin-top:8px">有效期至 2026-11-30</div></div>
      </div>
    </div>
    <div class="card" style="padding:0"><div style="padding:16px 16px 8px"><h3 style="margin:0">我的复购提醒</h3></div>
      <table><thead><tr><th>商品</th><th>上次购买</th><th>预计用完</th><th>提醒方式</th><th>操作</th></tr></thead>
      <tbody>
        <tr><td><b>爱他美卓萃 3 段 800g</b></td><td class="num">2026-10-06</td><td class="num">2026-11-05</td><td>站内消息 + 电话</td><td><a class="btn link">去复购</a></td></tr>
        <tr><td><b>帮宝适一级帮 L 码 42 片</b></td><td class="num">2026-10-12</td><td class="num">2026-11-08</td><td>站内消息</td><td><a class="btn link">去复购</a></td></tr>
      </tbody></table></div>
  </div>
  <div class="grid" style="gap:16px">
    <div class="card" style="background:linear-gradient(150deg,#C86B85,#D98BA0);color:#fff">
      <div style="font-size:12px;opacity:.9">我的积分</div>
      <div style="font-size:34px;font-weight:700;letter-spacing:-1px">1,286</div>
      <div style="font-size:12px;opacity:.9;margin-top:4px">金卡会员 · 累计消费 ¥3,286.50</div>
      <div style="display:flex;gap:16px;margin-top:16px;font-size:11.5px;opacity:.95">
        <span>本月获得 328</span><span>本月使用 60</span></div>
    </div>
    <div class="card"><h3>积分兑换</h3>
      <div class="kv"><span class="k">1,000 分</span><span class="v">兑换 满 99 减 10 券</span></div>
      <div class="kv"><span class="k">2,000 分</span><span class="v">兑换 纸尿裤试用装</span></div>
      <div class="kv"><span class="k">500 分</span><span class="v">兑换 婴儿湿巾 1 包</span></div>
      <button class="btn ghost" style="width:100%;margin-top:12px">去积分商城</button></div>
    <div class="card"><h3>待办提醒</h3>
      <div class="kv"><span class="k">待支付</span><span class="v">1 单 · 剩 18 分钟自动取消</span></div>
      <div class="kv"><span class="k">待收货</span><span class="v">2 单</span></div>
      <div class="kv"><span class="k">待评价</span><span class="v">3 单</span></div>
    </div>
  </div>
</div>"""
    return portal_shell("我的中心", "my.html", body)


PAGES = {
    "login.html": page_login,
    "dashboard.html": page_dashboard,
    "member.html": page_member,
    "baby.html": page_baby,
    "goods.html": page_goods,
    "coupon.html": page_coupon,
    "grant.html": page_grant,
    "order.html": page_order,
    "repurchase.html": page_repurchase,
    "churn.html": page_churn,
    "churn-rule.html": page_churn_rule,
    "report.html": page_report,
    "review.html": page_review,
    "sys.html": page_sys,
    "mall.html": page_mall,
    "checkout.html": page_checkout,
    "my.html": page_my,
}

if __name__ == "__main__":
    with open(os.path.join(BASE, "assets", "style.css"), "w", encoding="utf-8") as f:
        f.write(CSS)
    with open(os.path.join(BASE, "icons.svg"), "w", encoding="utf-8") as f:
        f.write(ICONS)
    for name, fn in PAGES.items():
        html = fn()
        with open(os.path.join(BASE, name), "w", encoding="utf-8") as f:
            f.write(html)
        with open(os.path.join(BASE, "pages", name), "w", encoding="utf-8") as f:
            f.write(html)
    print("生成 %s 个页面" % len(PAGES))
