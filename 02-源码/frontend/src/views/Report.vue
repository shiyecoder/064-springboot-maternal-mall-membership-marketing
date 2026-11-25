<template>
  <div>
    <div class="page-head">
      <h1>营销报表</h1>
      <p>口径按订单完成时间归属自然月，已退款订单从销售额与积分中剔除</p>
      <div class="acts">
        <el-button @click="load">刷新数据</el-button>
        <el-button type="primary" @click="exportCsv">导出 CSV</el-button>
      </div>
    </div>

    <div class="k5">
      <div class="kpi"><div class="lb">券领取量</div><div class="vv">{{ kpi.received }}</div><div class="df">覆盖 {{ couponRows.length }} 个券活动</div></div>
      <div class="kpi"><div class="lb">券核销量</div><div class="vv">{{ kpi.used }}</div><div class="df">核销率 {{ kpi.rate }}%</div></div>
      <div class="kpi"><div class="lb">券带动销售额</div><div class="vv">¥{{ money(kpi.sales) }}</div><div class="df">含券订单实付合计</div></div>
      <div class="kpi"><div class="lb">券 ROI</div><div class="vv">{{ kpi.roi }}</div><div class="df">带动销售额 / 券成本</div></div>
      <div class="kpi"><div class="lb">积分沉淀比</div><div class="vv">{{ point.ratio }}<small>: 1</small></div>
        <div class="df" :style="{ color: point.ratio > 1.5 ? 'var(--warn)' : 'var(--muted)' }">{{ point.ratio > 1.5 ? '超阈值 1.5' : '处于健康区间' }}</div></div>
    </div>

    <div class="mid">
      <div class="card">
        <div class="card-h">券活动效果<span class="more">按领取量降序</span></div>
        <el-table :data="couponRows" size="small" max-height="330">
          <el-table-column prop="couponName" label="券活动" min-width="170" show-overflow-tooltip>
            <template #default="{ row }"><b>{{ row.couponName }}</b></template>
          </el-table-column>
          <el-table-column prop="couponType" label="类型" width="62" />
          <el-table-column prop="received" label="领取" width="66" align="right" />
          <el-table-column prop="used" label="核销" width="66" align="right" />
          <el-table-column label="核销率" width="76" align="right">
            <template #default="{ row }"><b>{{ row.rate }}%</b></template>
          </el-table-column>
          <el-table-column label="带动销售额" width="96" align="right">
            <template #default="{ row }">¥{{ money(row.sales) }}</template>
          </el-table-column>
          <el-table-column label="券成本" width="84" align="right">
            <template #default="{ row }">¥{{ money(row.cost) }}</template>
          </el-table-column>
          <el-table-column prop="roi" label="ROI" width="64" align="right" />
        </el-table>
        <div class="mini">带动销售额只统计使用了该券且已完成的订单实付金额，退款订单不计入。</div>
      </div>

      <div>
        <div class="card">
          <div class="card-h">积分健康度</div>
          <div class="bars">
            <div class="r"><span class="nm">发放</span><span class="tr"><i :style="{ width: '100%' }"></i></span><span class="no">{{ point.gain }}</span></div>
            <div class="r"><span class="nm">消耗</span><span class="tr"><i :style="{ width: pct(point.use, point.gain) }"></i></span><span class="no">{{ point.use }}</span></div>
            <div class="r"><span class="nm">过期</span><span class="tr"><i :style="{ width: pct(point.expire, point.gain) }"></i></span><span class="no">{{ point.expire }}</span></div>
            <div class="r"><span class="nm">沉淀</span><span class="tr"><i :style="{ width: pct(point.stock, point.gain) }"></i></span><span class="no">{{ point.stock }}</span></div>
          </div>
          <div class="alert" v-if="point.ratio > 1.5">沉淀比 {{ point.ratio }} 已超阈值 1.5，建议上线「积分兑纸尿裤试用装」消化沉淀。</div>
          <div class="kv" style="margin-top:10px"><span class="k">会员复购率</span><span class="v">{{ overview.repeatRate || 0 }}%</span></div>
          <div class="kv"><span class="k">券核销率</span><span class="v">{{ overview.verifyRate || 0 }}%</span></div>
          <div class="kv"><span class="k">高风险会员</span><span class="v">{{ overview.highRisk || 0 }} 人</span></div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-h">宝宝月龄段购买分布<span class="more">按订单明细件数统计</span></div>
      <div ref="ageRef" class="chart"></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { crud, dashboardApi } from '@/api'

const crudCoupon = crud('coupon')
const crudReceive = crud('coupon-receive')
const crudOrder = crud('mall-order')
const crudPoint = crud('point-log')

const coupons = ref([])
const receives = ref([])
const orders = ref([])
const points = ref([])
const overview = ref({})
const ageDist = ref([])
const ageRef = ref(null)
let chart = null

const couponRows = computed(() => {
  return coupons.value.map((c) => {
    const mine = receives.value.filter((r) => r.couponId === c.id)
    const used = mine.filter((r) => r.useStatus === '已使用')
    const usedIds = new Set(used.map((r) => r.id))
    const sales = orders.value
      .filter((o) => o.status === '已完成' && usedIds.has(o.couponReceiveId))
      .reduce((s, o) => s + Number(o.payAmount || 0), 0)
    const cost = used.length * Number(c.discountAmount || 0)
    return {
      couponName: c.couponName, couponType: c.couponType,
      received: mine.length, used: used.length,
      rate: mine.length ? Math.round((used.length * 1000) / mine.length) / 10 : 0,
      sales: Math.round(sales), cost: Math.round(cost),
      roi: cost ? Math.round((sales / cost) * 10) / 10 : 0
    }
  }).sort((a, b) => b.received - a.received)
})

const kpi = computed(() => {
  const received = receives.value.length
  const used = receives.value.filter((r) => r.useStatus === '已使用').length
  const sales = couponRows.value.reduce((s, r) => s + r.sales, 0)
  const cost = couponRows.value.reduce((s, r) => s + r.cost, 0)
  return {
    received, used,
    rate: received ? Math.round((used * 1000) / received) / 10 : 0,
    sales, roi: cost ? Math.round((sales / cost) * 10) / 10 : 0
  }
})

const point = computed(() => {
  let gain = 0, use = 0, expire = 0
  for (const p of points.value) {
    const v = Number(p.changePoint || 0)
    if (v > 0) gain += v
    else if (p.changeType && p.changeType.includes('过期')) expire += -v
    else use += -v
  }
  const stock = Math.max(0, gain - use - expire)
  // 沉淀比 = 累计发放 / 累计消耗，>1.5 说明积分只进不出，需要做兑换活动
  return {
    gain, use, expire, stock,
    ratio: use ? Math.round((gain / use) * 10) / 10 : 0
  }
})

function pct(a, b) { return Math.min(100, Math.round(((a || 0) * 100) / (b || 1))) + '%' }
function money(n) { return Number(n || 0).toLocaleString('zh-CN', { maximumFractionDigits: 0 }) }

function drawAge() {
  if (!chart) return
  chart.setOption({
    grid: { left: 40, right: 16, top: 20, bottom: 30 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ageDist.value.map((d) => d.name), axisLine: { lineStyle: { color: '#e3e6e8' } }, axisLabel: { color: '#5b6770', fontSize: 11 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f2f3' } }, axisLabel: { color: '#5b6770', fontSize: 11 } },
    series: [{ type: 'bar', data: ageDist.value.map((d) => d.value), barWidth: 26, itemStyle: { color: '#c86b85', borderRadius: [4, 4, 0, 0] } }]
  })
}

async function load() {
  const [c, r, o, p, ov, ch] = await Promise.all([
    crudCoupon.page({ page: 1, size: 100 }),
    crudReceive.page({ page: 1, size: 500 }),
    crudOrder.page({ page: 1, size: 500 }),
    crudPoint.page({ page: 1, size: 500 }),
    dashboardApi.overview(),
    dashboardApi.charts()
  ])
  coupons.value = c.data.records || []
  receives.value = r.data.records || []
  orders.value = o.data.records || []
  points.value = p.data.records || []
  overview.value = ov.data || {}
  ageDist.value = ch.data.ageDist || []
  drawAge()
}

function exportCsv() {
  if (!couponRows.value.length) { ElMessage.warning('暂无可导出的数据'); return }
  const head = ['券活动', '类型', '领取', '核销', '核销率', '带动销售额', '券成本', 'ROI']
  const body = couponRows.value.map((r) => [r.couponName, r.couponType, r.received, r.used, r.rate + '%', r.sales, r.cost, r.roi])
  const csv = '\ufeff' + [head, ...body].map((r) => r.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(',')).join('\n')
  const a = document.createElement('a')
  a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }))
  a.download = `营销报表_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
  ElMessage.success('已导出 CSV')
}

onMounted(async () => {
  await load()
  chart = echarts.init(ageRef.value)
  drawAge()
  window.addEventListener('resize', () => chart && chart.resize())
})
onBeforeUnmount(() => { chart && chart.dispose(); chart = null })
</script>

<style scoped>
.k5 { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; margin-bottom: 16px; }
.kpi { background: #fff; border-radius: 8px; box-shadow: var(--sh); padding: 13px 15px; }
.kpi .lb { font-size: 12.5px; color: var(--muted); }
.kpi .vv { font-size: 23px; font-weight: 600; line-height: 1.3; font-variant-numeric: tabular-nums; }
.kpi .vv small { font-size: 12px; font-weight: 400; color: var(--muted); }
.kpi .df { font-size: 11.5px; color: var(--muted); }
.mid { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 16px; align-items: start; margin-bottom: 16px; }
.bars { margin-top: 4px; }
.bars .r { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; font-size: 12.5px; }
.bars .nm { width: 40px; color: var(--muted); flex: 0 0 40px; }
.bars .tr { flex: 1; height: 8px; background: #f0f2f3; border-radius: 4px; overflow: hidden; }
.bars .tr i { display: block; height: 100%; background: var(--primary-2); border-radius: 4px; }
.bars .no { width: 52px; text-align: right; font-variant-numeric: tabular-nums; }
.alert { background: #fdf0e4; color: #a9651c; border-radius: 8px; padding: 10px 12px; font-size: 12px; margin-top: 12px; line-height: 1.6; }
.mini { font-size: 11.5px; color: var(--muted); margin-top: 10px; line-height: 1.6; }
.chart { height: 220px; }
</style>
