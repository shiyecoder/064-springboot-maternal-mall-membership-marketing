<template>
  <div>
    <div class="page-head">
      <h1>发券任务</h1>
      <p>按会员等级与流失风险定向发放，同一会员同一券活动最多 1 张，已领过的自动跳过且不占用总量</p>
      <div class="acts">
        <el-button @click="loadCoupons">刷新券活动</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">发起定向发券</el-button>
      </div>
    </div>

    <div class="two">
      <div class="card">
        <div class="card-h">筛选目标会员</div>
        <el-form label-width="88px">
          <el-form-item label="券活动">
            <el-select v-model="form.couponId" style="width:100%" placeholder="选择发放中的券活动">
              <el-option v-for="c in usable" :key="c.id" :value="c.id"
                :label="`${c.couponName}（剩余 ${c.totalQty - c.receivedQty} 张）`" />
            </el-select>
          </el-form-item>
          <el-form-item label="会员等级">
            <el-select v-model="form.levels" multiple collapse-tags collapse-tags-tooltip style="width:100%" placeholder="不限">
              <el-option v-for="l in ['黑卡', '金卡', '银卡', '普通会员']" :key="l" :label="l" :value="l" />
            </el-select>
          </el-form-item>
          <el-form-item label="流失风险">
            <el-select v-model="form.risks" multiple collapse-tags collapse-tags-tooltip style="width:100%" placeholder="不限">
              <el-option v-for="r in ['高风险', '中风险', '低风险', '已流失', '未评估']" :key="r" :label="r" :value="r" />
            </el-select>
          </el-form-item>
          <el-form-item label="购买间隔">
            <el-select v-model="form.minGapDays" style="width:100%">
              <el-option :value="0" label="不限" />
              <el-option :value="30" label="30 天以上未购买" />
              <el-option :value="60" label="60 天以上未购买" />
              <el-option :value="90" label="90 天以上未购买" />
            </el-select>
          </el-form-item>
          <el-form-item label="本次上限">
            <el-input-number v-model="form.limit" :min="1" :max="5000" :step="50" />
            <span class="hint">张，留空按剩余总量发完</span>
          </el-form-item>
        </el-form>

        <div class="pre">
          <div><span>预计覆盖会员</span><b class="hl">{{ preview.length }} 人</b></div>
          <div><span>预计券成本</span><b>¥{{ cost.toFixed(2) }}</b></div>
          <div><span>发放后剩余</span><b>{{ remainAfter }} 张</b></div>
          <div class="hint2">成本按券面额 × 覆盖人数估算，实际以核销为准；超出券总量时按剩余数量截断。</div>
        </div>
      </div>

      <div class="card">
        <div class="card-h">券活动<span class="more">{{ usable.length }} 个发放中</span></div>
        <el-table :data="usable" size="small" highlight-current-row @current-change="pick" max-height="330">
          <el-table-column prop="couponName" label="券活动" min-width="150" show-overflow-tooltip />
          <el-table-column prop="couponType" label="类型" width="62" />
          <el-table-column label="面额" width="82" align="right">
            <template #default="{ row }">¥{{ Number(row.discountAmount).toFixed(0) }}</template>
          </el-table-column>
          <el-table-column label="剩余/总量" width="96" align="right">
            <template #default="{ row }">{{ row.totalQty - row.receivedQty }} / {{ row.totalQty }}</template>
          </el-table-column>
        </el-table>

        <div class="card-h" style="margin-top:16px">本次名单预览<span class="more">前 8 条</span></div>
        <el-table :data="preview.slice(0, 8)" size="small" max-height="230">
          <el-table-column prop="memberName" label="会员" width="88">
            <template #default="{ row }"><b>{{ row.memberName || row.nickName }}</b></template>
          </el-table-column>
          <el-table-column prop="levelCode" label="等级" width="80" />
          <el-table-column prop="riskLevel" label="风险" width="80">
            <template #default="{ row }">
              <el-tag size="small" effect="light" :type="row.riskLevel === '高风险' ? 'danger' : row.riskLevel === '中风险' ? 'warning' : 'success'">
                {{ row.riskLevel || '未评估' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="mobile" label="手机号" width="112">
            <template #default="{ row }">{{ mask(row.mobile) }}</template>
          </el-table-column>
        </el-table>
        <div class="mini">短信通道为模拟，不真实下发；记录统一写入领券表，可在优惠券明细中核对。</div>
      </div>
    </div>

    <el-dialog v-model="result.on" title="发放结果" width="420px">
      <div class="res">
        <div><span>实际发放</span><b class="hl">{{ result.data.granted }} 张</b></div>
        <div><span>跳过（已领过）</span><b>{{ result.data.skipped }} 人</b></div>
        <div><span>命中目标</span><b>{{ result.data.targets }} 人</b></div>
        <div><span>券成本</span><b>¥{{ Number(result.data.cost || 0).toFixed(2) }}</b></div>
        <div><span>剩余库存</span><b>{{ result.data.remainQty }} 张</b></div>
      </div>
      <template #footer><el-button type="primary" @click="result.on = false">知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { couponApi, crud } from '@/api'

const crudCoupon = crud('coupon')
const crudMember = crud('member')

const coupons = ref([])
const members = ref([])
const submitting = ref(false)
const result = reactive({ on: false, data: {} })
const form = reactive({ couponId: null, levels: [], risks: [], minGapDays: 0, limit: 200 })

const usable = computed(() => coupons.value.filter((c) => c.status === '发放中'))
const selected = computed(() => coupons.value.find((c) => c.id === form.couponId))

/** 前端先本地预估，最终以后端 grant() 的统计为准，避免两边口径漂移 */
const preview = computed(() => {
  const now = Date.now()
  return members.value.filter((m) => {
    if (m.status !== '正常') return false
    if (form.levels.length && !form.levels.includes(m.levelCode)) return false
    if (form.risks.length && !form.risks.includes(m.riskLevel || '未评估')) return false
    if (form.minGapDays > 0) {
      if (!m.lastBuyTime) return false
      const gap = (now - new Date(m.lastBuyTime).getTime()) / 86400000
      if (gap < form.minGapDays) return false
    }
    return true
  })
})

const cost = computed(() => {
  const unit = Number(selected.value?.discountAmount || 0)
  return Math.min(preview.value.length, form.limit || preview.value.length) * unit
})

const remainAfter = computed(() => {
  const c = selected.value
  if (!c) return 0
  return Math.max(0, c.totalQty - c.receivedQty - Math.min(preview.value.length, form.limit || preview.value.length))
})

function mask(s) { return s ? s.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '—' }
function pick(row) { if (row) form.couponId = row.id }

async function loadCoupons() {
  const [c, m] = await Promise.all([
    crudCoupon.page({ page: 1, size: 100 }),
    crudMember.page({ page: 1, size: 500 })
  ])
  coupons.value = c.data.records || []
  members.value = m.data.records || []
  if (!form.couponId && usable.value.length) form.couponId = usable.value[0].id
}

async function submit() {
  if (!form.couponId) { ElMessage.warning('请选择券活动'); return }
  if (!preview.value.length) { ElMessage.warning('当前筛选条件下没有可发放的会员'); return }
  submitting.value = true
  try {
    const r = await couponApi.grant(form.couponId, {
      levels: form.levels, risks: form.risks,
      minGapDays: form.minGapDays || null, limit: form.limit || null
    })
    result.data = r.data
    result.on = true
    await loadCoupons()
  } finally {
    submitting.value = false
  }
}

onMounted(loadCoupons)
</script>

<style scoped>
.two { display: grid; grid-template-columns: 420px minmax(0, 1fr); gap: 16px; align-items: start; }
.hint { font-size: 11.5px; color: var(--muted); margin-left: 10px; }
.mini { font-size: 11.5px; color: var(--muted); margin-top: 10px; line-height: 1.6; }
.pre { background: #fafbfb; border: 1px solid var(--line); border-radius: 9px; padding: 12px 14px; }
.pre > div { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 7px; }
.pre > div:last-child { margin-bottom: 0; }
.pre span { color: var(--muted); }
.pre .hl { color: var(--primary); font-size: 15px; }
.pre .hint2 { display: block; font-size: 11.5px; color: var(--muted); line-height: 1.6; border-top: 1px dashed var(--line); padding-top: 8px; margin-top: 2px; }
.res > div { display: flex; justify-content: space-between; padding: 9px 0; border-bottom: 1px dashed var(--line); font-size: 13px; }
.res > div:last-child { border-bottom: none; }
.res span { color: var(--muted); }
.res .hl { color: var(--primary); font-size: 16px; }
</style>
