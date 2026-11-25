<template>
  <div>
    <div class="page-head">
      <h1>复购提醒台</h1>
      <p>按预计用完日期前 3 天生成，同一会员同一商品 7 天内最多一条；奶粉按 26g/天、纸尿裤按 6 片/天估算</p>
      <div class="acts">
        <el-button :loading="generating" @click="generate">批量生成提醒</el-button>
        <el-button type="primary" @click="exportCsv">导出跟进名单</el-button>
      </div>
    </div>

    <div class="split">
      <div class="card list-card">
        <div class="card-h">待跟进提醒<span class="more">{{ total }} 条</span></div>
        <el-select v-model="sendStatus" size="small" style="width:100%;margin-bottom:10px" @change="load">
          <el-option value="" label="全部状态" />
          <el-option value="待发送" label="待发送" />
          <el-option value="已发送" label="已发送" />
        </el-select>
        <div class="list" v-loading="loading">
          <div v-for="t in rows" :key="t.id" class="li" :class="{ on: current?.id === t.id }" @click="current = t">
            <div class="t1">{{ t.memberName }} · {{ t.goodsName }}</div>
            <div class="t2">
              预计 {{ t.predictEndDate }} 用完
              <span class="tagx" :class="left(t) <= 3 ? 'dan' : 'warn'">{{ left(t) >= 0 ? `剩 ${left(t)} 天` : `逾期 ${-left(t)} 天` }}</span>
            </div>
          </div>
          <el-empty v-if="!rows.length && !loading" description="暂无待跟进提醒" :image-size="60" />
        </div>
        <el-pagination small layout="prev, pager, next" :total="total" :page-size="size"
          :current-page="page" @current-change="(p) => { page = p; load() }" style="margin-top:10px;justify-content:center" />
        <div class="mini">已退订短信的会员只允许站内消息与导购电话触达。</div>
      </div>

      <div v-if="current" class="right">
        <div class="card">
          <div class="card-h">
            {{ current.memberName }} · {{ current.goodsName }}
            <span class="more">{{ current.remindNo }}</span>
          </div>
          <div class="g2">
            <div>
              <div class="kv"><span class="k">会员</span><span class="v">{{ current.memberName }}</span></div>
              <div class="kv"><span class="k">最近购买</span><span class="v">{{ current.lastBuyDate }}</span></div>
              <div class="kv"><span class="k">预计用完</span><span class="v">{{ current.predictEndDate }}　<span class="tagx warn">{{ left(current) }} 天</span></span></div>
              <div class="kv"><span class="k">估算依据</span><span class="v">{{ basis }}</span></div>
            </div>
            <div>
              <div class="kv"><span class="k">提醒方式</span><span class="v">{{ current.remindChannel }}</span></div>
              <div class="kv"><span class="k">计划发送</span><span class="v">{{ dt(current.planTime) }}</span></div>
              <div class="kv"><span class="k">发送状态</span><span class="v">{{ current.sendStatus }}</span></div>
              <div class="kv"><span class="k">负责人</span><span class="v">{{ current.ownerName || '未分配' }}</span></div>
            </div>
          </div>
          <div class="kv"><span class="k">跟进结果</span><span class="v">{{ current.responseResult || '尚未登记' }}</span></div>
        </div>

        <div class="card">
          <div class="card-h">登记跟进</div>
          <el-form label-width="72px">
            <el-form-item label="结果">
              <el-radio-group v-model="follow.responseResult">
                <el-radio-button v-for="r in ['已回购', '未回应', '暂无需求', '已流失']" :key="r" :value="r">{{ r }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="follow.remark" type="textarea" :rows="2" maxlength="100" show-word-limit
                placeholder="例如：客户反馈宝宝已转 4 段，推荐了卓萃 4 段" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="following" @click="submitFollow">保存跟进</el-button>
              <el-button @click="gotoGrant">给该会员发券</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div v-else class="card"><el-empty description="从左侧选择一条提醒查看详情" /></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { crud, repurchaseApi } from '@/api'

const crudTask = crud('repurchase-task')
const router = useRouter()

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = 20
const sendStatus = ref('')
const loading = ref(false)
const generating = ref(false)
const following = ref(false)
const current = ref(null)
const follow = reactive({ responseResult: '已回购', remark: '' })

const basis = computed(() => {
  const n = current.value?.goodsName || ''
  if (n.includes('纸尿裤') || n.includes('拉拉裤')) return '日均 6 片，按片数 ÷ 6 估算可用天数'
  if (n.includes('奶粉')) return '日均 26g，按净含量 ÷ 26g 估算可用天数'
  return '按商品净含量与历史购买周期估算'
})

function left(t) {
  if (!t?.predictEndDate) return 0
  return Math.ceil((new Date(t.predictEndDate).getTime() - Date.now()) / 86400000)
}
function dt(s) { return (s || '').replace('T', ' ').slice(0, 16) }

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (sendStatus.value) params.sendStatus = sendStatus.value
    const r = await crudTask.page(params)
    rows.value = r.data.records || []
    total.value = r.data.total || 0
    if (!current.value || !rows.value.some((x) => x.id === current.value.id)) current.value = rows.value[0] || null
  } finally {
    loading.value = false
  }
}

async function generate() {
  generating.value = true
  try {
    const r = await repurchaseApi.generate()
    const d = r.data || {}
    ElMessage.success(`生成 ${d.created ?? 0} 条提醒，跳过 ${d.skipped ?? 0} 条（7 天内已存在）`)
    page.value = 1
    await load()
  } finally {
    generating.value = false
  }
}

async function submitFollow() {
  if (!current.value) return
  if (!follow.remark.trim()) { ElMessage.warning('请填写跟进备注'); return }
  following.value = true
  try {
    await repurchaseApi.follow(current.value.id, { responseResult: follow.responseResult, remark: follow.remark.trim() })
    ElMessage.success('跟进结果已登记')
    follow.remark = ''
    await load()
  } finally {
    following.value = false
  }
}

function gotoGrant() { router.push('/grant') }

function exportCsv() {
  if (!rows.value.length) { ElMessage.warning('当前没有可导出的记录'); return }
  const head = ['提醒编号', '会员', '商品', '最近购买', '预计用完', '提醒方式', '发送状态', '跟进结果', '负责人']
  const body = rows.value.map((t) => [t.remindNo, t.memberName, t.goodsName, t.lastBuyDate, t.predictEndDate, t.remindChannel, t.sendStatus, t.responseResult || '', t.ownerName || ''])
  const csv = '\ufeff' + [head, ...body].map((r) => r.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(',')).join('\n')
  const a = document.createElement('a')
  a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }))
  a.download = `复购跟进名单_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
  ElMessage.success('已导出 CSV')
}

onMounted(load)
</script>

<style scoped>
.split { display: grid; grid-template-columns: 340px minmax(0, 1fr); gap: 16px; align-items: start; }
.list-card { padding: 16px; }
.list { max-height: 460px; overflow: auto; }
.li { padding: 9px 11px; border-radius: 7px; cursor: pointer; border: 1px solid transparent; }
.li:hover { background: #fafbfb; }
.li.on { background: #fdf2f5; border-color: #f0cfd9; }
.li .t1 { font-size: 13px; font-weight: 500; }
.li .t2 { font-size: 11.5px; color: var(--muted); margin-top: 2px; }
.tagx { display: inline-block; font-size: 11px; padding: 0 5px; border-radius: 4px; margin-left: 4px; }
.tagx.warn { background: #fdf0e4; color: #a9651c; }
.tagx.dan { background: #fbe9eb; color: #b02a37; }
.right { display: grid; gap: 16px; }
.g2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 24px; }
.mini { font-size: 11.5px; color: var(--muted); margin-top: 8px; line-height: 1.6; }
:deep(.el-pagination) { display: flex; }
</style>
