/**
 * 全局时间格式化：全系统统一调用，禁止各页面自行拼格式。
 * 后端已按 yyyy-MM-dd HH:mm:ss 输出，这里主要处理缺失值与相对时间。
 */
export function formatDateTime(v) {
  if (!v) return '-'
  return String(v).replace('T', ' ').slice(0, 16)
}

/** 完整时间（含秒），仅详情抽屉等宽场景使用 */
export function formatFullTime(v) {
  if (!v) return '-'
  return String(v).replace('T', ' ').slice(0, 19)
}

export function formatDate(v) {
  if (!v) return '-'
  return String(v).slice(0, 10)
}

export function formatTime(v) {
  if (!v) return '-'
  return String(v).slice(0, 8)
}

/** 时段显示：14:00-17:00 */
export function formatRange(start, end) {
  if (!start || !end) return '-'
  return `${formatTime(start).slice(0, 5)}-${formatTime(end).slice(0, 5)}`
}

/** 相对时间：3 分钟前 / 昨天 */
export function fromNow(v) {
  if (!v) return '-'
  const t = new Date(String(v).replace(' ', 'T')).getTime()
  const diff = Date.now() - t
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 172800000) return '昨天'
  return formatDate(v)
}

export function money(v) {
  if (v === null || v === undefined || v === '') return '-'
  return Number(v).toFixed(2)
}

/** 剩余天数，负数表示已过期 */
export function daysLeft(dateStr) {
  if (!dateStr) return null
  const d = new Date(String(dateStr).slice(0, 10) + 'T00:00:00')
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return Math.round((d.getTime() - today.getTime()) / 86400000)
}

/** 状态 → Element Plus tag 类型，全系统唯一映射 */
const TAG = {
  有效: 'success', 已通过: 'success', 正常: 'success', 已闭环: 'success', 允许: 'success', 启用: 'success', 已入库: 'success',
  临期: 'warning', 待审核: 'warning', 待整改: 'warning', 较大: 'warning', 待发证: 'primary', 答题中: 'warning',
  已过期: 'danger', 已吊销: 'danger', 拒绝: 'danger', 重大: 'danger', 已逾期: 'danger', 已驳回: 'danger', 停用: 'danger', 已封闭: 'danger',
  使用中: 'primary', 待验收: 'primary', 整改中: 'primary', 已交卷: 'primary', 一级: 'success', 二级: 'primary',
  三级: 'warning', 四级: 'danger', 已完成: 'info', 已取消: 'info', 待考试: 'info', 作废: 'info', 一般: 'info',
  维修中: 'warning', 已封存: 'info', 已用完: 'info', 草稿: 'info', 已停用: 'info', 已归档: 'info', 生效中: 'success'
}
export function tagType(s) {
  return TAG[s] || 'info'
}
