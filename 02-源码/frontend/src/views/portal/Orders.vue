<template>
  <div>
    <div class="head">
      <h2>我的订单</h2>
      <div class="filters">
        <span v-for="s in tabs" :key="s" class="ft" :class="{ on: status === s }" @click="status = s">{{ s }}</span>
      </div>
      <span class="cnt">共 {{ list.length }} 笔</span>
    </div>

    <div v-loading="loading">
      <div v-for="o in list" :key="o.id" class="order">
        <div class="oh">
          <span class="no">订单号 {{ o.orderNo }}</span>
          <span class="tm">{{ (o.createTime || '').replace('T', ' ').slice(0, 16) }}</span>
          <span class="st" :class="cls(o.status)">{{ o.status }}</span>
        </div>
        <div class="ob">
          <div class="items">
            <div v-for="it in o.items" :key="it.id" class="it">
              <img :src="cover(it.goodsId)" :alt="it.goodsName" />
              <div>
                <div class="nm">{{ it.goodsName }}</div>
                <div class="sub">{{ it.specDesc }} · ¥{{ Number(it.price).toFixed(2) }} × {{ it.qty }}</div>
              </div>
            </div>
          </div>
          <div class="amt">
            <div class="sub">共 {{ o.itemCount }} 件　优惠 ¥{{ Number(o.discountAmount || 0).toFixed(2) }}　运费 ¥{{ Number(o.freight || 0).toFixed(2) }}</div>
            <div class="pay">实付 <b>¥{{ Number(o.payAmount).toFixed(2) }}</b></div>
            <div class="sub">获得积分 {{ o.gainPoint || 0 }}</div>
          </div>
        </div>
        <div class="of">
          <span class="rcv">收货：{{ o.receiverName }} {{ o.receiverMobile }} {{ o.receiverAddress }}</span>
          <div class="acts">
            <el-button v-if="o.status === '待支付'" size="small" type="primary" @click="pay(o)">立即支付</el-button>
            <el-button v-if="['待支付', '待发货'].includes(o.status)" size="small" @click="cancel(o)">取消订单</el-button>
            <el-button v-if="o.status === '已发货'" size="small" type="primary" @click="complete(o)">确认收货</el-button>
            <el-button v-if="['已发货', '已完成'].includes(o.status)" size="small" @click="applyReturn(o)">申请退货</el-button>
            <el-button v-if="o.status === '已完成'" size="small" @click="openReview(o)">评价</el-button>
            <span v-if="o.status === '退货中'" class="sub">等待门店审核退款</span>
            <span v-if="o.status === '已退款'" class="sub">已退款 ¥{{ Number(o.payAmount).toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && !list.length" description="该状态下暂无订单" />
    </div>

    <el-dialog v-model="rv.on" title="评价商品" width="460px">
      <el-form label-width="70px">
        <el-form-item label="商品">{{ rv.goodsName }}</el-form-item>
        <el-form-item label="评分"><el-rate v-model="rv.score" /></el-form-item>
        <el-form-item label="评价">
          <el-input v-model="rv.content" type="textarea" :rows="3" maxlength="200" show-word-limit
            placeholder="说说宝宝的使用感受，例如：口感好、易冲泡" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rv.on = false">取消</el-button>
        <el-button type="primary" :loading="rv.saving" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, portalApi, crud } from '@/api'
import { loadPortal, portal, resetPortal } from '@/store/portal'

const loading = ref(false)
const status = ref('全部')
const tabs = ['全部', '待支付', '待发货', '已发货', '已完成', '退货中']
const goodsCache = ref({})
const rv = reactive({ on: false, saving: false, order: null, goodsId: null, goodsName: '', score: 5, content: '' })

const orders = computed(() => portal.data?.orders || [])
const list = computed(() => (status.value === '全部' ? orders.value : orders.value.filter((o) => o.status === status.value)))

function cls(s) {
  return s === '待支付' ? 'warn' : s === '待发货' ? 'pri' : s === '已发货' ? 'ok'
    : s === '已完成' ? 'done' : s === '退货中' ? 'warn' : 'mute'
}
function cover(goodsId) { return goodsCache.value[goodsId] || '' }

async function load() {
  loading.value = true
  try {
    await loadPortal(true)
    const ids = new Set()
    ;(portal.data?.orders || []).forEach((o) => (o.items || []).forEach((i) => ids.add(i.goodsId)))
    if (ids.size) {
      const r = await crud('baby-goods').page({ page: 1, size: 100 })
      const map = {}
      ;(r.data.records || []).forEach((g) => { map[g.id] = g.coverImage })
      goodsCache.value = map
    }
  } finally {
    loading.value = false
  }
}

async function pay(o) {
  await orderApi.pay(o.id, '微信')
  ElMessage.success('支付成功，积分已入账')
  resetPortal()
  await load()
}

async function cancel(o) {
  const r = await ElMessageBox.prompt('请填写取消原因（不少于 5 个字）', '取消订单', {
    inputType: 'textarea', inputPlaceholder: '例如：地址填错，重新下单', confirmButtonText: '确认取消', cancelButtonText: '返回'
  }).catch(() => null)
  if (!r) return
  if (!r.value || r.value.trim().length < 5) { ElMessage.warning('取消原因不少于 5 个字'); return }
  await orderApi.cancel(o.id, r.value.trim())
  ElMessage.success('订单已取消，库存与优惠券已返还')
  resetPortal()
  await load()
}

async function complete(o) {
  await orderApi.complete(o.id)
  ElMessage.success('已确认收货，积分已入账')
  resetPortal()
  await load()
}

async function applyReturn(o) {
  const r = await ElMessageBox.prompt('请填写退货原因（不少于 5 个字）', '申请退货', {
    inputType: 'textarea', inputPlaceholder: '例如：宝宝转段位，奶粉未拆封', confirmButtonText: '提交申请', cancelButtonText: '返回'
  }).catch(() => null)
  if (!r) return
  if (!r.value || r.value.trim().length < 5) { ElMessage.warning('退货原因不少于 5 个字'); return }
  await orderApi.applyReturn(o.id, r.value.trim())
  ElMessage.success('退货申请已提交，等待门店审核')
  resetPortal()
  await load()
}

function openReview(o) {
  const item = (o.items || [])[0]
  rv.order = o
  rv.goodsId = item?.goodsId || null
  rv.goodsName = item?.goodsName || ''
  rv.score = 5
  rv.content = ''
  rv.on = true
}

async function submitReview() {
  if (!rv.content.trim()) { ElMessage.warning('请填写评价内容'); return }
  rv.saving = true
  try {
    await portalApi.review({
      orderId: rv.order.id, goodsId: rv.goodsId, score: rv.score, content: rv.content.trim()
    })
    ElMessage.success('评价已提交，审核通过后展示')
    rv.on = false
    resetPortal()
    await load()
  } finally {
    rv.saving = false
  }
}

onMounted(load)
</script>

<style scoped>
.head { display: flex; align-items: center; gap: 16px; margin-bottom: 14px; flex-wrap: wrap; }
.head h2 { font-size: 17px; margin: 0; }
.filters { display: flex; gap: 6px; }
.ft { padding: 4px 12px; border-radius: 20px; font-size: 12.5px; color: var(--muted); background: #fff; cursor: pointer; border: 1px solid transparent; }
.ft.on { color: var(--primary); background: #fdf2f5; border-color: #f0cfd9; font-weight: 600; }
.cnt { margin-left: auto; font-size: 12px; color: var(--muted); }

.order { background: #fff; border-radius: 10px; box-shadow: var(--sh); margin-bottom: 14px; }
.oh { display: flex; align-items: center; gap: 14px; padding: 11px 16px; border-bottom: 1px solid #f4f5f6; font-size: 12.5px; color: var(--muted); }
.oh .no { font-weight: 500; color: var(--text); }
.oh .st { margin-left: auto; font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.st.warn { background: #fdf0e4; color: #a9651c; }
.st.pri { background: #fdf2f5; color: var(--primary); }
.st.ok { background: #e9f4ee; color: #2b7955; }
.st.done { background: #eef2f4; color: #5b6770; }
.st.mute { background: #f3f4f5; color: #9aa4ac; }

.ob { display: flex; gap: 16px; padding: 14px 16px; }
.items { flex: 1; min-width: 0; display: grid; gap: 10px; }
.it { display: flex; gap: 10px; }
.it img { width: 54px; height: 54px; border-radius: 8px; object-fit: cover; background: #f3f5f6; }
.it .nm { font-size: 13px; }
.it .sub { font-size: 11.5px; color: var(--muted); margin-top: 2px; }
.amt { width: 220px; flex: 0 0 220px; text-align: right; border-left: 1px dashed var(--line); padding-left: 14px; }
.amt .pay { font-size: 13px; color: var(--muted); margin: 4px 0; }
.amt .pay b { font-size: 18px; color: var(--danger); }
.sub { font-size: 11.5px; color: var(--muted); }

.of { display: flex; align-items: center; gap: 12px; padding: 10px 16px; border-top: 1px solid #f4f5f6; flex-wrap: wrap; }
.rcv { font-size: 11.5px; color: var(--muted); }
.acts { margin-left: auto; display: flex; gap: 8px; align-items: center; }
</style>
