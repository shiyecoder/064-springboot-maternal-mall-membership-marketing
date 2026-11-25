<template>
  <div class="portal">
    <header class="ptop">
      <router-link to="/portal/mall" class="logo">
        <span class="mk">芽</span>
        芽芽母婴商城
      </router-link>
      <nav class="pnav">
        <router-link v-for="m in navs" :key="m.path" :to="m.path" :class="{ on: isOn(m) }">{{ m.name }}</router-link>
      </nav>
      <div class="right">
        <el-input v-if="showSearch" v-model="kw" class="sr" placeholder="搜索奶粉 / 纸尿裤 / 辅食" clearable
          @keyup.enter="doSearch" @clear="doSearch">
          <template #prefix><span class="sic">搜</span></template>
        </el-input>
        <el-badge :value="cartCount" :hidden="!cartCount" :offset="[-2, 4]">
          <span class="cart" @click="goCart">购物车</span>
        </el-badge>
        <el-dropdown @command="onCommand">
          <span class="who">
            <el-avatar :size="28" class="av">{{ initial }}</el-avatar>
            <span class="lv">{{ member?.levelCode || '会员' }}</span>
            {{ member?.nickName || user?.realName || '会员' }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="my">我的中心</el-dropdown-item>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item command="admin" v-if="canAdmin">进入后台</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="ticker" v-if="member">
      <span>当前宝宝 <b>{{ activeBaby ? `${activeBaby.babyName} · ${activeBaby.monthAge} 个月 · ${activeBaby.milkStage || ''} ${activeBaby.diaperSize || ''}` : '未建档' }}</b></span>
      <span class="dot">·</span>
      <span>积分余额 <b>{{ member.pointBalance || 0 }}</b></span>
      <span class="dot">·</span>
      <span>累计消费 <b>¥{{ Number(member.totalAmount || 0).toFixed(2) }}</b></span>
      <span class="sp"></span>
      <span class="mini">商品按宝宝月龄筛选适龄段，跨段位仍可下单</span>
    </div>

    <main class="pbody">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { cartCount, clearCart } from '@/store/cart'
import { loadPortal, portal, resetPortal } from '@/store/portal'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const kw = ref('')

const user = computed(() => store.user)
const member = computed(() => portal.data?.member)
const activeBaby = computed(() => (portal.data?.babies || []).find((b) => b.status === 1))
const initial = computed(() => (member.value?.nickName || user.value?.realName || '会').slice(0, 1))
const canAdmin = computed(() => store.roles.some((r) => ['ADMIN', 'MANAGER', 'OPERATOR', 'MARKETER', 'GUIDE'].includes(r)))
const showSearch = computed(() => route.path === '/portal/mall')

const navs = [
  { path: '/portal/mall', name: '首页' },
  { path: '/portal/my', name: '我的宝宝' },
  { path: '/portal/my', name: '我的优惠券', tab: 'coupon' },
  { path: '/portal/orders', name: '我的订单' },
  { path: '/portal/my', name: '积分中心', tab: 'point' }
]

function isOn(m) {
  if (m.tab) return route.path === m.path && route.query.tab === m.tab
  return route.path === m.path && !route.query.tab
}

function doSearch() {
  router.push({ path: '/portal/mall', query: kw.value ? { q: kw.value } : {} })
}
function goCart() { router.push('/portal/checkout') }

function onCommand(c) {
  if (c === 'logout') {
    clearCart()
    resetPortal()
    store.logout()
    router.push('/login')
  } else if (c === 'my') router.push('/portal/my')
  else if (c === 'orders') router.push('/portal/orders')
  else if (c === 'admin') router.push('/dashboard')
}

onMounted(async () => {
  if (!store.user) await store.fetchMe().catch(() => {})
  await loadPortal().catch(() => {})
})
</script>

<style scoped>
.portal { min-height: 100vh; background: var(--bg); }
.ptop {
  height: 64px; background: #fff; border-bottom: 1px solid var(--line);
  display: flex; align-items: center; padding: 0 40px; gap: 30px;
  position: sticky; top: 0; z-index: 20;
}
.logo { display: flex; align-items: center; gap: 9px; font-size: 16px; font-weight: 600; color: var(--text); white-space: nowrap; }
.logo .mk {
  width: 30px; height: 30px; border-radius: 8px; background: var(--primary);
  color: #fff; display: flex; align-items: center; justify-content: center; font-size: 15px;
}
.pnav { display: flex; gap: 22px; font-size: 14px; color: var(--muted); overflow: auto; }
.pnav a { padding: 21px 0; border-bottom: 2px solid transparent; white-space: nowrap; }
.pnav a.on { color: var(--primary); border-bottom-color: var(--primary); font-weight: 600; }
.pnav a:hover { color: var(--primary); }
.right { margin-left: auto; display: flex; align-items: center; gap: 14px; font-size: 13px; color: var(--muted); }
.sr { width: 210px; }
.sic { font-size: 11px; color: #9aa4ac; }
.cart { cursor: pointer; font-size: 13px; color: var(--muted); }
.cart:hover { color: var(--primary); }
.who { display: flex; align-items: center; gap: 7px; cursor: pointer; color: var(--text); outline: none; font-size: 13px; }
.av { background: var(--primary-2); font-size: 12px; }
.lv { font-size: 11px; color: var(--primary); background: #fdf2f5; border-radius: 4px; padding: 0 5px; }

.ticker {
  height: 34px; background: #fff; border-bottom: 1px solid var(--line);
  display: flex; align-items: center; gap: 8px; padding: 0 40px; font-size: 12px; color: var(--muted);
}
.ticker b { color: var(--text); font-weight: 600; }
.ticker .dot { color: #d9dee1; }
.ticker .sp { margin-left: auto; }
.ticker .mini { font-size: 11.5px; }

.pbody { max-width: 1180px; margin: 0 auto; padding: 20px 40px 60px; }

@media (max-width: 980px) {
  .ptop { padding: 0 14px; gap: 12px; }
  .pnav { gap: 12px; font-size: 13px; }
  .sr { display: none; }
  .ticker { padding: 0 14px; }
  .ticker .sp, .ticker .mini { display: none; }
  .pbody { padding: 14px; }
  .who .lv { display: none; }
}
</style>
