<template>
  <div class="dash">
    <div class="page-head">
      <h1>经营驾驶舱</h1>
      <p>数据截至 {{ today }}　·　口径：订单完成时间归属自然月，已退款订单剔除</p>
      <div class="acts">
        <el-radio-group v-model="range" size="small" @change="load">
          <el-radio-button label="30">近 30 天</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="quarter">本季度</el-radio-button>
        </el-radio-group>
        <el-button size="small" :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <div class="kpis">
      <div class="kpi hl" v-for="k in kpiList" :key="k.label">
        <div class="lb">{{ k.label }}</div>
        <div class="vv">{{ k.value }}<small v-if="k.unit">{{ k.unit }}</small></div>
        <div class="df" :class="{ down: k.down }">{{ k.note }}</div>
      </div>
    </div>

    <div class="charts">
      <div class="card c2"><div class="ch">近 30 天会员新增与复购<span>新增 / 复购会员数</span></div><div ref="c1" class="cv"></div></div>
      <div class="card"><div class="ch">会员等级分布<span>共 {{ totalMembers }} 人</span></div><div ref="c2" class="cv"></div></div>
      <div class="card"><div class="ch">各品类销售额</div><div ref="c3" class="cv"></div></div>
      <div class="card"><div class="ch">优惠券领取到核销漏斗</div><div ref="c4" class="cv"></div></div>
      <div class="card"><div class="ch">流失风险等级分布</div><div ref="c5" class="cv"></div></div>
      <div class="card"><div class="ch">宝宝月龄段购买分布</div><div ref="c6" class="cv"></div></div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { dashboardApi } from '@/api'

const today = new Date().toISOString().slice(0, 10)
const range = ref('30')
const loading = ref(false)
const ov = ref({})
const charts = ref({})
const refs = { c1: ref(), c2: ref(), c3: ref(), c4: ref(), c5: ref(), c6: ref() }
const c1 = refs.c1, c2 = refs.c2, c3 = refs.c3, c4 = refs.c4, c5 = refs.c5, c6 = refs.c6

const totalMembers = computed(() => ov.value.totalMembers || 0)
const kpiList = computed(() => [
  { label: '今日新增会员', value: ov.value.newToday ?? 0, unit: '人', note: `本月累计 ${ov.value.newMonth ?? 0} 人` },
  { label: '会员总数', value: ov.value.totalMembers ?? 0, unit: '人', note: '在册正常状态' },
  { label: '今日订单数', value: ov.value.todayOrders ?? 0, unit: '单', note: '含待支付与待发货' },
  { label: '今日销售额', value: '¥' + Number(ov.value.todaySales ?? 0).toLocaleString(), note: `本月 ¥${Number(ov.value.monthSales ?? 0).toLocaleString()}` },
  { label: '会员复购率', value: (ov.value.repeatRate ?? 0) + '%', note: '本月消费 2 次及以上占比', down: (ov.value.repeatRate ?? 0) < 40 },
  { label: '优惠券核销率', value: (ov.value.verifyRate ?? 0) + '%', note: '已使用 / 已领取' },
  { label: '高风险流失会员', value: ov.value.highRisk ?? 0, unit: '人', note: `待召回 ${ov.value.riskPending ?? 0} 人`, down: true },
  { label: '积分发放 / 消耗', value: ov.value.pointRatio ?? '-', note: '沉淀偏高时建议做兑换活动' }
])

const PALETTE = ['#C86B85', '#E3A0B4', '#7FB3A6', '#D9A24A', '#8E8AA8', '#D9DEE1']

function init(refEl, option) {
  if (!refEl) return
  const inst = echarts.getInstanceByDom(refEl) || echarts.init(refEl)
  inst.setOption(option, true)
  return inst
}

async function load() {
  loading.value = true
  try {
    const [a, b] = await Promise.all([dashboardApi.overview(), dashboardApi.charts()])
    ov.value = a.data || {}
    const d = b.data || {}
    await nextTick()
    charts.value.c1 = init(c1.value, {
      grid: { left: 8, right: 12, top: 18, bottom: 4, containLabel: true },
      tooltip: { trigger: 'axis' },
      legend: { right: 0, top: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 11 } },
      xAxis: { type: 'category', data: d.days || [], axisLine: { lineStyle: { color: '#E7E4E6' } }, axisLabel: { fontSize: 10, interval: 4 } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F1EFF0' } }, axisLabel: { fontSize: 10 } },
      series: [
        { name: '新增', type: 'line', smooth: true, symbol: 'none', data: d.newSeries || [], lineStyle: { width: 2, color: '#C86B85' }, itemStyle: { color: '#C86B85' } },
        { name: '复购', type: 'line', smooth: true, symbol: 'none', data: d.repeatSeries || [], lineStyle: { width: 2, color: '#7FB3A6' }, itemStyle: { color: '#7FB3A6' } }
      ]
    })
    const lv = d.levelDist || {}
    charts.value.c2 = init(c2.value, {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, itemWidth: 9, itemHeight: 9, textStyle: { fontSize: 10.5 } },
      series: [{
        type: 'pie', radius: ['48%', '72%'], center: ['50%', '44%'],
        label: { show: true, formatter: '{b}\n{c}人', fontSize: 10.5, lineHeight: 14 },
        labelLine: { length: 8, length2: 6 },
        data: Object.entries(lv).map(([k, v], i) => ({ name: k, value: v, itemStyle: { color: PALETTE[i] } }))
      }]
    })
    const cs = d.categorySales || {}
    charts.value.c3 = init(c3.value, {
      grid: { left: 8, right: 14, top: 14, bottom: 2, containLabel: true },
      tooltip: { trigger: 'axis', valueFormatter: (v) => '¥' + Number(v).toLocaleString() },
      xAxis: { type: 'category', data: Object.keys(cs), axisLabel: { fontSize: 10, interval: 0 }, axisLine: { lineStyle: { color: '#E7E4E6' } } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F1EFF0' } }, axisLabel: { fontSize: 10, formatter: (v) => v / 1000 + 'k' } },
      series: [{
        type: 'bar', barWidth: 20, data: Object.values(cs).map((v) => Number(v)),
        itemStyle: { borderRadius: [5, 5, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#E3A0B4' }, { offset: 1, color: '#F6DCE3' }]) }
      }]
    })
    const fn = d.couponFunnel || []
    charts.value.c4 = init(c4.value, {
      tooltip: { trigger: 'item' },
      series: [{
        type: 'funnel', left: '6%', right: '6%', top: 8, bottom: 4, minSize: '26%',
        label: { show: true, position: 'inside', fontSize: 11, color: '#fff', formatter: '{b} {c}' },
        itemStyle: { borderWidth: 0, borderRadius: 4 },
        data: fn.map((x, i) => ({ name: x.name, value: x.value, itemStyle: { color: PALETTE[i] } }))
      }]
    })
    const rd = d.riskDist || {}
    charts.value.c5 = init(c5.value, {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, itemWidth: 9, itemHeight: 9, textStyle: { fontSize: 10.5 } },
      series: [{
        type: 'pie', radius: '62%', center: ['50%', '44%'],
        label: { show: false },
        data: [
          { name: '低风险', value: rd['低风险'] || 0, itemStyle: { color: '#2B7955' } },
          { name: '中风险', value: rd['中风险'] || 0, itemStyle: { color: '#D9A24A' } },
          { name: '高风险', value: rd['高风险'] || 0, itemStyle: { color: '#B02A37' } },
          { name: '已流失', value: rd['已流失'] || 0, itemStyle: { color: '#8E8AA8' } }
        ]
      }]
    })
    const ad = d.ageDist || []
    charts.value.c6 = init(c6.value, {
      grid: { left: 8, right: 12, top: 14, bottom: 2, containLabel: true },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ad.map((x) => x.name), axisLabel: { fontSize: 10 }, axisLine: { lineStyle: { color: '#E7E4E6' } } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F1EFF0' } }, axisLabel: { fontSize: 10 } },
      series: [{ type: 'bar', barWidth: 26, data: ad.map((x) => x.value), itemStyle: { borderRadius: [5, 5, 0, 0], color: '#7FB3A6' } }]
    })
  } finally {
    loading.value = false
  }
}

function onResize() {
  Object.values(charts.value).forEach((c) => c && c.resize())
}

onMounted(load)
window.addEventListener('resize', onResize)
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  Object.values(charts.value).forEach((c) => c && c.dispose())
})
</script>

<style scoped>
.dash { display: flex; flex-direction: column; gap: 10px; }
.page-head { margin-bottom: 0; }
.page-head h1 { font-size: 18px; }
.page-head p { font-size: 12px; }
.kpis { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; }
.kpi {
  background: #fff; border: 1px solid var(--line); border-radius: 10px;
  padding: 10px 14px; box-shadow: var(--sh);
}
.kpi.hl { background: linear-gradient(135deg, #fff, #fbf3f5); border-color: #efd7de; }
.kpi .lb { font-size: 11.5px; color: var(--muted); }
.kpi .vv { font-size: 21px; font-weight: 700; letter-spacing: -0.5px; margin-top: 2px; }
.kpi .vv small { font-size: 11.5px; font-weight: 500; color: var(--muted); margin-left: 3px; }
.kpi .df { font-size: 11px; color: var(--ok); margin-top: 1px; }
.kpi .df.down { color: var(--danger); }
.charts { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
.card { padding: 10px 12px; border: 1px solid var(--line); border-radius: 10px; background: #fff; box-shadow: var(--sh); }
.card.c2 { grid-column: span 2; }
.ch { font-size: 12.5px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.ch span { margin-left: auto; font-size: 11px; color: var(--muted); font-weight: 400; }
.cv { height: 168px; width: 100%; }
</style>
