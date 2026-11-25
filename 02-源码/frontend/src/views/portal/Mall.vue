<template>
  <div v-loading="loading">
    <!-- 宝宝切换条 -->
    <div class="babybar">
      <span class="mini">当前宝宝</span>
      <span v-for="(b, i) in home.babies" :key="b.id" class="b" :class="{ on: b.status === 1 }" @click="pickBaby(i)">
        {{ b.babyName }} · {{ b.monthAge }} 个月
      </span>
      <router-link to="/portal/my" class="b off">+ 添加宝宝</router-link>
      <span class="sp"></span>
      <span class="mini">{{ home.monthAge ? `已按 ${home.monthAge} 月龄筛选适龄商品` : '未建档，暂按全部在售商品展示' }}</span>
    </div>

    <div class="banner">
      <div class="bt">
        <h2>秋冬换季，纸尿裤囤货季</h2>
        <p>{{ home.member?.levelCode || '会员' }}专享：满 199 免运费，满 300 减 50，叠加券后 L 码低至 122 元 / 包</p>
        <div class="bacts">
          <el-button type="primary" @click="scrollTo('纸尿裤')">立即选购</el-button>
          <el-button @click="goCoupon">领取优惠券（{{ claimable.length }} 张可领）</el-button>
        </div>
      </div>
      <div class="ring"><i></i></div>
    </div>

    <!-- 可领优惠券 -->
    <div v-if="claimable.length" class="sec-title">可领优惠券 <small>同一活动限领 1 张</small></div>
    <div v-if="claimable.length" class="coupons">
      <div v-for="c in claimable" :key="c.id" class="cp">
        <div class="cp-l">
          <b>¥{{ Number(c.discountAmount).toFixed(0) }}</b>
          <span>满 {{ Number(c.minAmount).toFixed(0) }} 可用</span>
        </div>
        <div class="cp-r">
          <div class="nm">{{ c.couponName }}</div>
          <div class="sub">{{ (c.validEnd || '').slice(0, 10) }} 到期 · {{ c.limitType }}</div>
        </div>
        <el-button size="small" :loading="claiming === c.id" @click="receive(c)">领取</el-button>
      </div>
    </div>

    <!-- 商品分组 -->
    <template v-for="(list, type) in filteredGoods" :key="type">
      <div class="sec-title" :id="`sec-${type}`">{{ type }}
        <small>{{ typeHint(type) }}</small>
        <span class="cnt">{{ list.length }} 件</span>
      </div>
      <div class="pgrid">
        <div v-for="g in list" :key="g.id" class="pcard" :class="{ sold: g.status !== '在售' }">
          <div class="ph">
            <img :src="img(g.coverImage)" :alt="g.goodsName" loading="lazy" />
            <span v-if="g.status !== '在售'" class="mask">已售罄</span>
          </div>
          <div class="pb">
            <div class="pn">{{ g.goodsName }}</div>
            <div class="psub">{{ g.brand }} · {{ g.specDesc }}</div>
            <div class="pp"><b>¥{{ Number(g.memberPrice ?? g.salePrice).toFixed(2) }}</b><s>¥{{ Number(g.salePrice).toFixed(2) }}</s>
              <span class="stk">库存 {{ g.stockQty }}</span></div>
            <div class="pfoot">
              <span class="tag">{{ g.ageMonthStart }}-{{ g.ageMonthEnd }} 月</span>
              <span class="fit" v-if="home.monthAge >= g.ageMonthStart && home.monthAge <= g.ageMonthEnd">适龄</span>
              <el-button size="small" text type="primary" :disabled="g.status !== '在售'" @click="add(g)">加购</el-button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <el-empty v-if="!loading && !Object.keys(filteredGoods).length" description="没有匹配的商品，换个关键词试试" />

    <div class="notices" v-if="home.notices?.length">
      <div class="nt-t">门店公告</div>
      <div v-for="n in home.notices" :key="n.id" class="nt"><b>{{ n.title }}</b><span>{{ n.content }}</span></div>
    </div>

    <div class="cartbar" v-if="cartCount">
      <span>购物车 {{ cartCount }} 件</span>
      <b>¥{{ cartAmount.toFixed(2) }}</b>
      <el-button type="primary" size="small" @click="goCheckout">去结算</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { portalApi, crud } from '@/api'
import { addToCart, cartCount, cartAmount } from '@/store/cart'
import { loadPortal, resetPortal } from '@/store/portal'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const home = ref({})
const loading = ref(false)
const claiming = ref(null)
const kw = ref(route.query.q || '')

watch(() => route.query.q, (v) => { kw.value = v || '' })

const claimable = computed(() => {
  const got = new Set(home.value.receivedCouponIds || [])
  return (home.value.coupons || []).filter((c) => !got.has(c.id))
})

const filteredGoods = computed(() => {
  const src = home.value.goodsByType || {}
  const q = (kw.value || '').trim()
  if (!q) return src
  const out = {}
  Object.entries(src).forEach(([k, list]) => {
    const hit = list.filter((g) => (g.goodsName + g.brand + g.goodsType).includes(q))
    if (hit.length) out[k] = hit
  })
  return out
})

function img(u) { return u || '' }
function typeHint(t) {
  if (t === '奶粉') return '按段位推荐'
  if (t === '纸尿裤') return '按体重选码'
  if (t === '辅食') return '按月龄选质地'
  return '门店现货'
}
function scrollTo(t) {
  const el = document.getElementById(`sec-${t}`)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
function goCoupon() {
  const el = document.querySelector('.coupons')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}
function goCheckout() { router.push('/portal/checkout') }

async function load() {
  loading.value = true
  try {
    const r = await portalApi.home()
    home.value = r.data
  } finally {
    loading.value = false
  }
}

async function receive(c) {
  claiming.value = c.id
  try {
    await portalApi.receive(c.id)
    ElMessage.success(`已领取「${c.couponName}」，可在我的优惠券查看`)
    resetPortal()
    await load()
    await loadPortal(true).catch(() => {})
  } finally {
    claiming.value = null
  }
}

function add(g) {
  addToCart(g, 1)
  ElMessage.success(`已加入购物车：${g.goodsName}`)
}

/** 切换当前宝宝：把该宝宝置为在看，其余置为归档，影响适龄筛选 */
async function pickBaby(i) {
  const b = home.value.babies[i]
  if (!b || b.status === 1) return
  await crud('baby-profile').edit(b.id, { ...b, status: 1 })
  const others = home.value.babies.filter((x) => x.id !== b.id)
  for (const o of others) await crud('baby-profile').edit(o.id, { ...o, status: 0 })
  ElMessage.success(`已切换到 ${b.babyName}`)
  resetPortal()
  await load()
}

onMounted(load)
</script>

<style scoped>
.babybar {
  display: flex; align-items: center; gap: 8px; background: #fff; border-radius: 10px;
  box-shadow: var(--sh); padding: 10px 14px; margin-bottom: 14px; font-size: 12.5px; overflow: auto;
}
.babybar .mini { color: var(--muted); font-size: 11.5px; white-space: nowrap; }
.babybar .b { padding: 3px 10px; border-radius: 20px; background: #f5f6f7; color: var(--muted); white-space: nowrap; cursor: pointer; }
.babybar .b.on { background: #fdf2f5; color: var(--primary); font-weight: 600; }
.babybar .b.off { background: #fff; border: 1px dashed var(--line); }
.babybar .sp { margin-left: auto; }

.banner {
  background: linear-gradient(120deg, #f5d9e1, #fbeef1 60%, #f7f2ee);
  border-radius: 12px; padding: 26px 30px; margin-bottom: 18px;
  display: flex; align-items: center; justify-content: space-between; overflow: hidden;
}
.banner h2 { font-size: 21px; margin: 0 0 6px; color: #7f3d52; }
.banner p { font-size: 13px; color: #8b5b6b; margin: 0; }
.bacts { display: flex; gap: 10px; margin-top: 16px; }
.ring { width: 92px; height: 92px; border-radius: 50%; border: 10px solid rgba(255, 255, 255, 0.55); position: relative; flex: 0 0 92px; }
.ring i { position: absolute; left: 34px; top: -17px; width: 20px; height: 20px; border-radius: 4px; background: rgba(255, 255, 255, 0.6); }

.sec-title { display: flex; align-items: baseline; gap: 8px; font-size: 15px; font-weight: 600; margin: 20px 0 12px; }
.sec-title small { font-size: 12px; color: var(--muted); font-weight: 400; }
.sec-title .cnt { margin-left: auto; font-size: 12px; color: var(--muted); font-weight: 400; }

.coupons { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 12px; }
.cp { display: flex; align-items: center; gap: 12px; background: #fff; border-radius: 10px; box-shadow: var(--sh); padding: 12px 14px; }
.cp-l { width: 86px; flex: 0 0 86px; text-align: center; border-right: 1px dashed var(--line); }
.cp-l b { display: block; font-size: 22px; color: var(--primary); line-height: 1.1; }
.cp-l span { font-size: 11px; color: var(--muted); }
.cp-r { flex: 1; min-width: 0; }
.cp-r .nm { font-size: 13px; font-weight: 500; }
.cp-r .sub { font-size: 11.5px; color: var(--muted); margin-top: 2px; }

.pgrid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.pcard { background: #fff; border-radius: 10px; box-shadow: var(--sh); overflow: hidden; display: flex; flex-direction: column; }
.pcard.sold { opacity: 0.72; }
.ph { height: 138px; background: #f3f5f6; position: relative; }
.ph img { width: 100%; height: 100%; object-fit: cover; display: block; }
.mask { position: absolute; inset: 0; background: rgba(31, 41, 51, 0.42); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 13px; }
.pb { padding: 10px 12px 11px; flex: 1; display: flex; flex-direction: column; }
.pn { font-size: 13px; font-weight: 500; line-height: 1.35; height: 36px; overflow: hidden; }
.psub { font-size: 11.5px; color: var(--muted); margin: 2px 0 6px; }
.pp { display: flex; align-items: baseline; gap: 6px; }
.pp b { font-size: 16px; color: var(--danger); }
.pp s { font-size: 11.5px; color: #9aa4ac; }
.pp .stk { margin-left: auto; font-size: 11px; color: var(--muted); }
.pfoot { display: flex; align-items: center; gap: 6px; margin-top: 7px; }
.tag { font-size: 11px; background: #fdf2f5; color: var(--primary); border-radius: 4px; padding: 1px 6px; }
.fit { font-size: 11px; color: var(--ok); }
.pfoot :deep(.el-button) { margin-left: auto; }

.notices { margin-top: 24px; background: #fff; border-radius: 10px; box-shadow: var(--sh); padding: 14px 16px; }
.nt-t { font-size: 13px; font-weight: 600; margin-bottom: 8px; }
.nt { display: flex; gap: 10px; font-size: 12.5px; color: var(--muted); padding: 4px 0; }
.nt b { color: var(--text); font-weight: 500; white-space: nowrap; }

.cartbar {
  position: fixed; right: 40px; bottom: 28px; background: #fff; border-radius: 12px;
  box-shadow: 0 6px 20px rgba(31, 41, 51, 0.16); padding: 12px 16px;
  display: flex; align-items: center; gap: 12px; font-size: 13px; z-index: 30;
}
.cartbar b { color: var(--danger); font-size: 16px; }

@media (max-width: 980px) {
  .pgrid { grid-template-columns: repeat(2, 1fr); }
  .banner { padding: 18px; }
  .ring { display: none; }
  .cartbar { right: 14px; bottom: 14px; }
}
</style>
