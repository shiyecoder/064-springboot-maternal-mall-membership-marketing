<template>
  <div>
    <div class="page-head">
      <h1>会员分层看板</h1>
      <p>每晚 02:00 重算流失风险分与等级　·　规则版本 {{ active.ruleVersion || '-' }}（{{ (active.effectTime || '').slice(0, 10) || '未生效' }}）</p>
      <div class="acts">
        <el-button :loading="calculating" @click="recalcAll">重算全部</el-button>
        <el-button type="primary" @click="openAssign()">分配到导购</el-button>
      </div>
    </div>

    <!-- 四个等级：人数 + 占比 -->
    <div class="lv-grid">
      <div v-for="l in levels" :key="l.name" class="lv">
        <div class="lb"><i :style="{ background: l.color }"></i>{{ l.name }}</div>
        <div class="vv">{{ l.count }}<small>人 · {{ l.pct }}%</small></div>
        <div class="df">{{ l.desc }}</div>
      </div>
    </div>

    <div class="tri">
      <div class="card">
        <div class="card-h">流失风险分布<span class="more">{{ board.total || 0 }} 人</span></div>
        <div class="donut" :style="{ background: donutBg }">
          <div class="ctr"><b>{{ highPct }}%</b><span>高风险占比</span></div>
        </div>
        <div class="legend">
          <div><i style="background:#b02a37"></i>高风险 {{ risk('高风险') + risk('已流失') }} 人</div>
          <div><i style="background:#d98324"></i>中风险 {{ risk('中风险') }} 人</div>
          <div><i style="background:#2b7955"></i>低风险 {{ risk('低风险') }} 人</div>
          <div><i style="background:#d9dee1"></i>未评估 {{ risk('未评估') }} 人</div>
        </div>
      </div>

      <div class="card">
        <div class="card-h">召回效果<span class="more">当前快照</span></div>
        <div class="bars">
          <div class="r"><span class="nm">已召回</span><span class="tr"><i :style="{ width: bar(board.recallDone, maxRecall) }"></i></span><span class="no">{{ board.recallDone || 0 }}</span></div>
          <div class="r"><span class="nm">跟进中</span><span class="tr"><i :style="{ width: bar(board.followCount, maxRecall) }"></i></span><span class="no">{{ board.followCount || 0 }}</span></div>
          <div class="r"><span class="nm">待召回</span><span class="tr"><i :style="{ width: bar(board.recallPending, maxRecall) }"></i></span><span class="no">{{ board.recallPending || 0 }}</span></div>
          <div class="r"><span class="nm">召回完成率</span><span class="tr"><i :style="{ width: recallRate + '%' }"></i></span><span class="no">{{ recallRate }}%</span></div>
        </div>
        <div class="mini">人工修正 {{ board.adjustCount || 0 }} 次　·　修正率 {{ board.adjustRate || 0 }}%</div>
      </div>

      <div class="card">
        <div class="card-h">待召回优先级说明</div>
        <div class="kv"><span class="k">高风险 ≥{{ active.highThreshold || 75 }}</span><span class="v">48 小时内导购电话召回 + 定向满减券</span></div>
        <div class="kv"><span class="k">中风险 {{ active.midThreshold || 50 }}-{{ (active.highThreshold || 75) - 1 }}</span><span class="v">7 天内推送复购提醒与适龄商品</span></div>
        <div class="kv"><span class="k">低风险 &lt;{{ active.midThreshold || 50 }}</span><span class="v">按月推送复购提醒，不额外发券</span></div>
        <div class="kv"><span class="k">人工修正</span><span class="v">可修正一次，原因不少于 5 字，原值保留</span></div>
        <div class="mini">分值越高流失风险越大；距今天数在 90 天内分段线性归一。</div>
      </div>
    </div>

    <div class="card">
      <div class="card-h">
        待召回名单
        <span class="more" style="cursor:default">共 {{ rows.total }} 条</span>
      </div>
      <div class="filters">
        <span class="fi">风险等级</span>
        <el-select v-model="query.riskLevel" placeholder="全部" clearable style="width: 140px" @change="load">
          <el-option v-for="r in ['高风险', '中风险', '低风险', '已流失']" :key="r" :label="r" :value="r" />
        </el-select>
        <span class="fi">召回状态</span>
        <el-select v-model="query.recallStatus" placeholder="全部" clearable style="width: 140px" @change="load">
          <el-option v-for="r in ['待召回', '跟进中', '已召回', '已放弃']" :key="r" :label="r" :value="r" />
        </el-select>
        <span class="spacer"></span>
        <el-button text type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows.records" v-loading="loading" size="default">
        <el-table-column prop="memberName" label="会员" min-width="88">
          <template #default="{ row }"><b>{{ row.memberName }}</b></template>
        </el-table-column>
        <el-table-column label="五因素得分" min-width="272">
          <template #default="{ row }">
            <span class="fac">间隔 {{ row.scoreGap }}</span><span class="fac">频次 {{ row.scoreFreq }}</span>
            <span class="fac">客单 {{ row.scoreAmount }}</span><span class="fac">券 {{ row.scoreCoupon }}</span>
            <span class="fac">月龄 {{ row.scoreAge }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="风险分" width="96" align="right" sortable header-align="center">
          <template #default="{ row }">
            <b class="num" :style="{ color: row.totalScore >= 75 ? 'var(--danger)' : row.totalScore >= 50 ? 'var(--warn)' : 'var(--muted)' }">{{ row.totalScore }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="86">
          <template #default="{ row }"><el-tag :type="tagOf(row.riskLevel)" size="small" effect="light">{{ row.riskLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="recallStatus" label="召回状态" width="86" />
        <el-table-column prop="ownerName" label="负责人" width="84">
          <template #default="{ row }">{{ row.ownerName || '未分配' }}</template>
        </el-table-column>
        <el-table-column prop="calcTime" label="计算时间" width="118">
          <template #default="{ row }">{{ (row.calcTime || '').replace('T', ' ').slice(5, 16) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="152" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openAdjust(row)">修正</el-button>
            <el-button link type="primary" size="small" @click="openAssign(row)">分配</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :total="rows.total" :page-size="query.size"
          :current-page="query.page" @current-change="(p) => { query.page = p; load() }" />
      </div>
    </div>

    <!-- 召回详情 -->
    <el-drawer v-model="detail.on" title="召回详情" size="460px">
      <div v-if="detail.row">
        <div class="drawer-sub">{{ detail.row.memberName }} · {{ detail.row.riskLevel }} · 风险分 {{ detail.row.totalScore }} · 规则 {{ detail.row.ruleVersion }}</div>
        <div class="kv"><span class="k">结论</span><span class="v">{{ detail.row.conclusion || '—' }}</span></div>
        <div class="kv"><span class="k">未满足</span><span class="v">{{ detail.row.unmet || '—' }}</span></div>
        <div class="kv"><span class="k">建议动作</span><span class="v">{{ detail.row.suggest || '—' }}</span></div>
        <div class="kv"><span class="k">输入快照</span><span class="v"><code class="snap">{{ pretty(detail.row.inputJson) }}</code></span></div>
        <div class="kv"><span class="k">召回状态</span><span class="v">{{ detail.row.recallStatus }} · {{ detail.row.ownerName || '未分配' }}</span></div>
        <div class="kv"><span class="k">计算时间</span><span class="v">{{ (detail.row.calcTime || '').replace('T', ' ').slice(0, 19) }}</span></div>
        <el-divider content-position="left">修正记录</el-divider>
        <el-empty v-if="!myAdjust.length" description="暂无修正记录" :image-size="60" />
        <div v-for="a in myAdjust" :key="a.id" class="adj">
          <div><b>{{ a.originLevel }} → {{ a.newLevel }}</b><span class="mini">{{ (a.adjustTime || '').replace('T', ' ').slice(0, 16) }}</span></div>
          <div class="mini">{{ a.reason }}</div>
        </div>
      </div>
    </el-drawer>

    <!-- 人工修正 -->
    <el-dialog v-model="adjust.on" title="人工修正风险等级" width="440px">
      <el-alert type="warning" :closable="false" show-icon
        title="同一快照只能修正一次，原风险等级与分值保留在修正记录中，不覆盖算法快照。" />
      <el-form label-width="80px" style="margin-top:14px">
        <el-form-item label="会员"><b>{{ adjust.row?.memberName }}</b> · 当前 {{ adjust.row?.riskLevel }}（{{ adjust.row?.totalScore }} 分）</el-form-item>
        <el-form-item label="修正为">
          <el-select v-model="adjust.newLevel" style="width:100%">
            <el-option v-for="r in ['高风险', '中风险', '低风险', '已流失']" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="修正原因">
          <el-input v-model="adjust.reason" type="textarea" :rows="3" maxlength="120" show-word-limit
            placeholder="不少于 5 个字，例如：门店已电话确认客户搬迁外地" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjust.on = false">取消</el-button>
        <el-button type="primary" :loading="adjust.saving" @click="submitAdjust">提交修正</el-button>
      </template>
    </el-dialog>

    <!-- 分配导购 -->
    <el-dialog v-model="assign.on" title="分配召回负责人" width="400px">
      <el-form label-width="72px">
        <el-form-item label="会员">{{ assign.row?.memberName || `已选 ${assign.ids.length} 条` }}</el-form-item>
        <el-form-item label="导购">
          <el-select v-model="assign.ownerId" filterable placeholder="选择导购" style="width:100%">
            <el-option v-for="u in users" :key="u.id" :label="`${u.realName} · ${u.username}`" :value="u.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assign.on = false">取消</el-button>
        <el-button type="primary" :loading="assign.saving" @click="submitAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { churnApi } from '@/api'
import { crud } from '@/api'

const crudUser = crud('user')
const loading = ref(false)
const calculating = ref(false)
const board = ref({})
const rows = ref({ records: [], total: 0 })
const users = ref([])
const adjusts = ref([])
const query = reactive({ page: 1, size: 10, riskLevel: '', recallStatus: '' })

const active = computed(() => board.value.rule || {})
const detail = reactive({ on: false, row: null })
const adjust = reactive({ on: false, row: null, newLevel: '', reason: '', saving: false })
const assign = reactive({ on: false, row: null, ids: [], ownerId: null, saving: false })

const levels = computed(() => {
  const d = board.value.levelDist || {}
  const total = board.value.total || 1
  return [
    { name: '黑卡', color: '#c86b85', count: d['黑卡'] || 0, pct: pct(d['黑卡'], total), desc: '¥8,000 以上 / 20 次以上' },
    { name: '金卡', color: '#e3a0b4', count: d['金卡'] || 0, pct: pct(d['金卡'], total), desc: '¥3,000 以上 / 10 次以上' },
    { name: '银卡', color: '#7fb3a6', count: d['银卡'] || 0, pct: pct(d['银卡'], total), desc: '¥1,000 以上 / 5 次以上' },
    { name: '普通会员', color: '#d9dee1', count: d['普通会员'] || 0, pct: pct(d['普通会员'], total), desc: '其他' }
  ]
})

function pct(n, total) { return Math.round(((n || 0) * 100) / (total || 1)) }
function risk(k) { return (board.value.riskDist || {})[k] || 0 }

const highPct = computed(() => pct(risk('高风险') + risk('已流失'), board.value.total))
const maxRecall = computed(() => Math.max(board.value.recallDone || 0, board.value.followCount || 0, board.value.recallPending || 0, 1))
const recallRate = computed(() => {
  const done = board.value.recallDone || 0
  const all = done + (board.value.followCount || 0) + (board.value.recallPending || 0)
  return all ? Math.round((done * 100) / all) : 0
})
function bar(v, max) { return Math.min(100, Math.round(((v || 0) * 100) / (max || 1))) + '%' }

/** 环形图用 conic-gradient 拼，避免为一张小图引入图表实例 */
const donutBg = computed(() => {
  const total = board.value.total || 1
  const h = pct(risk('高风险') + risk('已流失'), total)
  const m = pct(risk('中风险'), total)
  const l = pct(risk('低风险'), total)
  return `conic-gradient(#b02a37 0 ${h}%, #d98324 ${h}% ${h + m}%, #2b7955 ${h + m}% ${h + m + l}%, #d9dee1 ${h + m + l}% 100%)`
})

const myAdjust = computed(() => adjusts.value.filter((a) => a.memberId === detail.row?.memberId))

function tagOf(v) {
  if (v === '高风险' || v === '已流失') return 'danger'
  if (v === '中风险') return 'warning'
  if (v === '低风险') return 'success'
  return 'info'
}
function pretty(s) {
  if (!s) return '—'
  try { return JSON.stringify(JSON.parse(s), null, 1) } catch { return s }
}

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.riskLevel) params.riskLevel = query.riskLevel
    if (query.recallStatus) params.recallStatus = query.recallStatus
    const [b, p] = await Promise.all([churnApi.board(), churnApi.page(params)])
    board.value = b.data
    rows.value = p.data
  } finally {
    loading.value = false
  }
}

async function recalcAll() {
  calculating.value = true
  try {
    const r = await churnApi.calculateAll()
    ElMessage.success(`已重算 ${r.data} 位会员的风险分，历史快照保留`)
    await load()
  } finally {
    calculating.value = false
  }
}

async function openDetail(row) {
  detail.row = row
  detail.on = true
  if (!adjusts.value.length) adjusts.value = (await churnApi.adjusts()).data
}

function openAdjust(row) {
  if (row.recallStatus === '已修正') { ElMessage.warning('该快照已修正过，不能再次修正'); return }
  adjust.row = row
  adjust.newLevel = row.riskLevel
  adjust.reason = ''
  adjust.on = true
}

async function submitAdjust() {
  if (!adjust.reason || adjust.reason.trim().length < 5) { ElMessage.warning('修正原因不少于 5 个字'); return }
  adjust.saving = true
  try {
    await churnApi.adjust(adjust.row.id, { newLevel: adjust.newLevel, reason: adjust.reason.trim() })
    ElMessage.success('已修正，原值保留在修正记录')
    adjust.on = false
    adjusts.value = []
    await load()
  } finally {
    adjust.saving = false
  }
}

async function openAssign(row) {
  assign.row = row || null
  assign.ids = row ? [row.id] : []
  assign.ownerId = row?.ownerId || null
  assign.on = true
  if (!users.value.length) {
    const r = await crudUser.page({ page: 1, size: 100 })
    users.value = (r.data.records || []).filter((u) => ['导购', '店长'].includes(u.roleName) || u.roleId)
  }
}

async function submitAssign() {
  if (!assign.ownerId) { ElMessage.warning('请选择导购'); return }
  if (!assign.ids.length) { ElMessage.warning('请先选择待分配的会员'); return }
  assign.saving = true
  try {
    for (const id of assign.ids) await churnApi.assign(id, assign.ownerId)
    ElMessage.success(`已分配 ${assign.ids.length} 条召回任务`)
    assign.on = false
    await load()
  } finally {
    assign.saving = false
  }
}

onMounted(load)
</script>

<style scoped>
.lv-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 16px; }
.lv { background: #fff; border-radius: 8px; box-shadow: var(--sh); padding: 14px 16px; }
.lv .lb { display: flex; align-items: center; gap: 6px; font-size: 12.5px; color: var(--muted); }
.lv .lb i { width: 8px; height: 8px; border-radius: 3px; display: inline-block; }
.lv .vv { font-size: 26px; font-weight: 600; line-height: 1.25; margin-top: 2px; font-variant-numeric: tabular-nums; }
.lv .vv small { font-size: 12px; font-weight: 400; color: var(--muted); margin-left: 4px; }
.lv .df { font-size: 11.5px; color: var(--muted); }

.tri { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.donut { width: 132px; height: 132px; border-radius: 50%; margin: 6px auto 0; position: relative; }
.donut .ctr { position: absolute; inset: 30px; background: #fff; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.donut .ctr b { font-size: 22px; }
.donut .ctr span { font-size: 11px; color: var(--muted); }
.legend { display: grid; grid-template-columns: 1fr 1fr; gap: 6px 10px; margin-top: 14px; font-size: 12.5px; color: var(--muted); }
.legend div { display: flex; align-items: center; gap: 6px; }
.legend i { width: 8px; height: 8px; border-radius: 3px; }

.bars { margin-top: 4px; }
.bars .r { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; font-size: 12.5px; }
.bars .nm { width: 68px; color: var(--muted); flex: 0 0 68px; }
.bars .tr { flex: 1; height: 8px; background: #f0f2f3; border-radius: 4px; overflow: hidden; }
.bars .tr i { display: block; height: 100%; background: var(--primary-2); border-radius: 4px; }
.bars .no { width: 44px; text-align: right; font-variant-numeric: tabular-nums; color: var(--text); }
.mini { font-size: 11.5px; color: var(--muted); margin-top: 8px; }

.fac { display: inline-block; white-space: nowrap; font-size: 11px; color: var(--muted); background: #f6f7f8; border-radius: 4px; padding: 1px 4px; margin-right: 3px; }
.pager { display: flex; justify-content: flex-end; padding-top: 14px; }
.snap { display: block; white-space: pre-wrap; font-size: 11.5px; color: var(--muted); background: #fafbfb; border-radius: 6px; padding: 8px; }
.adj { border-left: 2px solid var(--primary-2); padding: 2px 0 2px 10px; margin-bottom: 10px; }
.adj > div:first-child { display: flex; gap: 8px; align-items: baseline; font-size: 13px; }
</style>
