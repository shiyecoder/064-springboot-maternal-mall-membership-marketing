<template>
  <div class="app">
    <aside class="side" :class="{ collapsed }">
      <div class="brand">
        <span class="dot"></span>
        <div v-show="!collapsed">
          芽芽母婴会员营销
          <small>MOM &amp; BABY CRM</small>
        </div>
      </div>
      <nav class="nav">
        <template v-for="g in groups" :key="g.key">
          <div class="grp" v-show="!collapsed">{{ g.key }}</div>
          <router-link
            v-for="m in g.items"
            :key="m.path"
            :to="m.path"
            class="item"
            :class="{ on: isActive(m.path) }"
          >
            <el-icon class="ic"><component :is="iconOf(m.icon)" /></el-icon>
            <span v-show="!collapsed">{{ m.menuName }}</span>
          </router-link>
        </template>
      </nav>
      <div class="side-foot" v-show="!collapsed">
        <div>杭州滨江星光店 · 会员运营部</div>
        <div style="opacity: 0.7">v1.0 · 通道3</div>
      </div>
    </aside>

    <div class="main">
      <header class="top">
        <el-icon class="fold" @click="collapsed = !collapsed"><Expand v-if="collapsed" /><Fold v-else /></el-icon>
        <div class="crumb">会员经营 / <b>{{ route.meta.crumb || route.meta.title }}</b></div>
        <div class="top-right">
          <el-badge :value="unread" :hidden="!unread" class="bdg">
            <el-icon class="i"><Bell /></el-icon>
          </el-badge>
          <span class="role-tag">{{ roleLabel }}</span>
          <el-dropdown @command="onCommand">
            <span class="who">
              <el-avatar :size="30" :src="avatarSrc" class="av">{{ initial }}</el-avatar>
              {{ user?.realName }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="portal" v-if="canPortal">切换到门户</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <div class="content">
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { crud } from '@/api'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const collapsed = ref(false)
const unread = ref(0)

const user = computed(() => store.user)
const initial = computed(() => (user.value?.realName || '用').slice(0, 1))
const canPortal = computed(() => store.roles.includes('MEMBER'))
const avatarSrc = computed(() => store.avatar || undefined)

const ROLE_CN = {
  ADMIN: '系统管理员',
  MANAGER: '门店店长',
  OPERATOR: '商城运营',
  MARKETER: '会员营销专员',
  GUIDE: '门店导购',
  MEMBER: '会员家长'
}
const roleLabel = computed(() => store.roles.map((r) => ROLE_CN[r] || r).join(' / ') || '未分配角色')

/** 菜单按后端返回的目录/菜单动态生成，分组名取目录名 */
const groups = computed(() => {
  const dirs = store.menus.filter((m) => m.type === 'M').sort((a, b) => a.sortNo - b.sortNo)
  const out = []
  for (const d of dirs) {
    const items = store.menus
      .filter((m) => m.type === 'C' && m.parentId === d.id)
      .sort((a, b) => a.sortNo - b.sortNo)
    if (items.length) out.push({ key: d.menuName, items })
  }
  return out
})

/** 菜单图标在 sys_menu.icon 里直接存 Element Plus 组件名，直接按名渲染即可 */
const iconOf = (k) => k || 'Menu'

const isActive = (p) => route.path === p || route.path.startsWith(p + '/')

function onCommand(c) {
  if (c === 'logout') {
    store.logout()
    router.push('/login')
  } else if (c === 'profile') {
    router.push('/profile')
  } else if (c === 'portal') {
    router.push('/portal/mall')
  }
}

onMounted(async () => {
  if (!store.menus.length) await store.fetchMe()
  try {
    const r = await crud('message').page({ page: 1, size: 20, readFlag: 0 })
    unread.value = r.data.total || 0
  } catch (e) {
    unread.value = 0
  }
})
</script>

<style scoped>
.app { display: flex; min-height: 100vh; }
.side {
  width: 220px; flex: 0 0 220px; background: linear-gradient(180deg, #ffffff, #ffffff);
  color: var(--text); display: flex; flex-direction: column;
  position: fixed; top: 0; bottom: 0; left: 0; z-index: 20; transition: width 0.16s;
}
.side.collapsed { width: 64px; flex: 0 0 64px; }
.brand {
  height: 56px; display: flex; align-items: center; gap: 10px; padding: 0 18px;
  border-bottom: 1px solid var(--line);
  font-weight: 600; color: var(--text); letter-spacing: 0.5px; font-size: 14px; white-space: nowrap;
}
.brand .dot { width: 10px; height: 10px; border-radius: 50%; background: var(--primary-2); flex: 0 0 10px; }
.brand small { display: block; font-size: 9.5px; color: #9aa4ac; font-weight: 400; letter-spacing: 0.5px; }
.nav { flex: 1; overflow: auto; padding: 10px 0; }
.nav .grp { padding: 12px 18px 6px; font-size: 11px; color: #9aa4ac; letter-spacing: 1px; }
.nav .item {
  display: flex; align-items: center; gap: 10px; padding: 9px 18px;
  font-size: 13.5px; color: var(--muted); border-left: 3px solid transparent; white-space: nowrap;
}
.nav .item:hover { background: #fbf3f5; color: var(--primary); }
.nav .item.on { background: #fdf2f5; color: var(--primary); border-left-color: var(--primary); font-weight: 600; }
.nav .item .ic { font-size: 15px; }
.side-foot { padding: 12px 18px; border-top: 1px solid var(--line); font-size: 11.5px; color: #9aa4ac; }
.main { margin-left: 220px; flex: 1; min-width: 0; display: flex; flex-direction: column; }
.top {
  height: 56px; background: #fff; border-bottom: 1px solid var(--line);
  display: flex; align-items: center; padding: 0 24px; gap: 14px;
  position: sticky; top: 0; z-index: 10;
}
.fold { font-size: 18px; color: var(--muted); cursor: pointer; }
.crumb { font-size: 13px; color: var(--muted); }
.crumb b { color: var(--text); font-weight: 600; }
.top-right { margin-left: auto; display: flex; align-items: center; gap: 16px; font-size: 13px; color: var(--muted); }
.bdg .i { font-size: 17px; }
.role-tag { font-size: 11.5px; padding: 3px 9px; border-radius: 100px; background: #f3efed; color: var(--primary); }
.who { display: flex; align-items: center; gap: 8px; cursor: pointer; color: var(--text); font-size: 13px; outline: none; }
.av { background: var(--primary-2); }
.content { padding: 24px; flex: 1; }
</style>
