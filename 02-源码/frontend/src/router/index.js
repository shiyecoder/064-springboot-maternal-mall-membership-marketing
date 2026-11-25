import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由表：后台（店长/运营/营销/导购/管理员）与会员门户两套骨架。
 * 页面级 meta.perm 仅用于前端兜底，真正的权限在后端复核（需求文档第 2 节）。
 */
const routes = [
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '经营驾驶舱', crumb: '经营驾驶舱' } },
      { path: 'member', name: 'member', component: () => import('@/views/CrudView.vue'), meta: { title: '会员档案', crumb: '会员档案', entity: 'member' } },
      { path: 'baby', name: 'baby', component: () => import('@/views/CrudView.vue'), meta: { title: '宝宝档案', crumb: '宝宝档案', entity: 'baby' } },
      { path: 'churn', name: 'churn', component: () => import('@/views/Churn.vue'), meta: { title: '会员分层看板', crumb: '会员分层看板' } },
      { path: 'churn-rule', name: 'churn-rule', component: () => import('@/views/ChurnRule.vue'), meta: { title: '流失规则配置', crumb: '流失规则配置' } },
      { path: 'goods', name: 'goods', component: () => import('@/views/CrudView.vue'), meta: { title: '商品管理', crumb: '商品管理', entity: 'goods' } },
      { path: 'review', name: 'review', component: () => import('@/views/CrudView.vue'), meta: { title: '评价审核', crumb: '评价审核', entity: 'review' } },
      { path: 'coupon', name: 'coupon', component: () => import('@/views/CrudView.vue'), meta: { title: '优惠券配置', crumb: '优惠券配置', entity: 'coupon' } },
      { path: 'grant', name: 'grant', component: () => import('@/views/Grant.vue'), meta: { title: '发券任务', crumb: '发券任务' } },
      { path: 'repurchase', name: 'repurchase', component: () => import('@/views/Repurchase.vue'), meta: { title: '复购提醒台', crumb: '复购提醒台' } },
      { path: 'report', name: 'report', component: () => import('@/views/Report.vue'), meta: { title: '营销报表', crumb: '营销报表' } },
      { path: 'order', name: 'order', component: () => import('@/views/CrudView.vue'), meta: { title: '订单管理', crumb: '订单管理', entity: 'order' } },
      { path: 'user', name: 'user', component: () => import('@/views/CrudView.vue'), meta: { title: '用户管理', crumb: '用户管理', entity: 'user' } },
      { path: 'role', name: 'role', component: () => import('@/views/RoleManage.vue'), meta: { title: '角色管理', crumb: '角色管理' } },
      { path: 'menu', name: 'menu', component: () => import('@/views/MenuManage.vue'), meta: { title: '菜单管理', crumb: '菜单管理' } },
      { path: 'dict', name: 'dict', component: () => import('@/views/DictManage.vue'), meta: { title: '字典管理', crumb: '字典管理' } },
      { path: 'oper-log', name: 'oper-log', component: () => import('@/views/CrudView.vue'), meta: { title: '操作日志', crumb: '操作日志', entity: 'operlog' } },
      { path: 'profile', name: 'profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人信息', crumb: '个人信息' } }
    ]
  },
  {
    path: '/portal',
    component: () => import('@/layout/PortalLayout.vue'),
    redirect: '/portal/mall',
    children: [
      { path: 'mall', name: 'p-mall', component: () => import('@/views/portal/Mall.vue'), meta: { title: '商城首页' } },
      { path: 'checkout', name: 'p-checkout', component: () => import('@/views/portal/Checkout.vue'), meta: { title: '结算' } },
      { path: 'my', name: 'p-my', component: () => import('@/views/portal/My.vue'), meta: { title: '我的中心' } },
      { path: 'orders', name: 'p-orders', component: () => import('@/views/portal/Orders.vue'), meta: { title: '我的订单' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.path === '/login' && token) return { path: '/' }
  return true
})

export default router
