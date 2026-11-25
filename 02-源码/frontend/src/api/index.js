import req from './request'

/** 通用 CRUD 工厂：所有列表/表单页面共用，避免每页重复实现分页、详情、增删改 */
export const crud = (base) => ({
  page: (params) => req.get(`/${base}/page`, { params }),
  detail: (id) => req.get(`/${base}/${id}`),
  add: (data) => req.post(`/${base}`, data),
  edit: (id, data) => req.put(`/${base}/${id}`, data),
  remove: (id) => req.delete(`/${base}/${id}`)
})

export const authApi = {
  login: (data) => req.post('/auth/login', data),
  me: () => req.get('/auth/me'),
  menus: () => req.get('/auth/menus'),
  changePassword: (data) => req.post('/auth/password', data),
  updateProfile: (data) => req.put('/auth/profile', data),
  logout: () => req.post('/auth/logout')
}

export const dictApi = {
  types: () => req.get('/dict/types'),
  all: () => req.get('/dict/all'),
  items: (t) => req.get(`/dict/${t}`)
}

export const dashboardApi = {
  overview: () => req.get('/dashboard/overview'),
  charts: () => req.get('/dashboard/charts')
}

/** 订单状态机：全部走后端 OrderService 校验，前端只发动作 */
export const orderApi = {
  create: (data) => req.post('/mall-order/create', data),
  pay: (id, payType) => req.post(`/mall-order/${id}/pay`, { payType }),
  ship: (id) => req.post(`/mall-order/${id}/ship`),
  complete: (id) => req.post(`/mall-order/${id}/complete`),
  cancel: (id, reason) => req.post(`/mall-order/${id}/cancel`, { reason }),
  applyReturn: (id, reason) => req.post(`/mall-order/${id}/apply-return`, { reason }),
  refund: (id, reason) => req.post(`/mall-order/${id}/refund`, { reason }),
  mine: () => req.get('/mall-order/mine')
}

export const churnApi = {
  rule: () => req.get('/churn/rule'),
  saveRule: (d) => req.post('/churn/rule', d),
  board: () => req.get('/churn/board'),
  page: (params) => req.get('/churn/page', { params }),
  calculate: (memberId) => req.post(`/churn/calculate/${memberId}`),
  calculateAll: () => req.post('/churn/calculate-all'),
  adjust: (scoreId, d) => req.post(`/churn/adjust/${scoreId}`, d),
  assign: (scoreId, ownerId) => req.post(`/churn/assign/${scoreId}?ownerId=${ownerId}`),
  adjusts: () => req.get('/churn/adjusts')
}

export const couponApi = {
  grant: (id, d) => req.post(`/coupon/${id}/grant`, d),
  expire: () => req.post('/coupon/expire'),
  receive: (couponId, memberId) => req.post(`/coupon-receive/receive/${couponId}?memberId=${memberId}`)
}

export const repurchaseApi = {
  generate: () => req.post('/repurchase-task/generate'),
  follow: (id, d) => req.post(`/repurchase-task/${id}/follow`, d)
}

export const portalApi = {
  home: () => req.get('/portal/home'),
  my: () => req.get('/portal/my'),
  coupons: () => req.get('/portal/my-coupons'),
  receive: (id) => req.post(`/portal/receive/${id}`),
  review: (d) => req.post('/portal/review', d)
}

export const roleApi = {
  menus: (id) => req.get(`/role/${id}/menus`),
  saveMenus: (id, ids) => req.put(`/role/${id}/menus`, ids)
}

export const menuApi = {
  tree: () => req.get('/menu/tree'),
  add: (d) => req.post('/menu', d),
  edit: (id, d) => req.put(`/menu/${id}`, d),
  remove: (id) => req.delete(`/menu/${id}`)
}

export const memberApi = {
  recalc: (id) => req.post(`/member/${id}/level-recalc`),
  churn: (id) => req.get(`/member/${id}/churn-score`)
}

export const reviewApi = {
  audit: (id, d) => req.post(`/goods-review/${id}/audit`, d)
}

/** CrudView 行操作使用的动作集合（键名与 config/entities.js 的 action.api 对应） */
export const actionApi = {
  shipOrder: (id) => orderApi.ship(id),
  completeOrder: (id) => orderApi.complete(id),
  cancelOrder: (id, d) => orderApi.cancel(id, d.reason),
  refundOrder: (id, d) => orderApi.refund(id, d.reason),
  recalcMember: (id) => memberApi.recalc(id),
  auditPass: (id) => reviewApi.audit(id, { status: '已通过' }),
  auditReject: (id, d) => reviewApi.audit(id, { status: '已驳回', reply: d.reply }),
  restockGoods: (id) => req.post(`/baby-goods/${id}/restock?qty=50`)
}
