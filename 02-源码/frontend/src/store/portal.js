import { reactive } from 'vue'
import { portalApi } from '@/api'

/**
 * 门户公共数据：会员资料、宝宝、券、订单、积分。
 * 顶栏与「我的中心」「我的订单」共用一次请求，避免每次切页重复拉全量。
 */
export const portal = reactive({ data: null, loaded: false, loading: false })

export async function loadPortal(force = false) {
  if (portal.loaded && !force) return portal.data
  if (portal.loading) return portal.data
  portal.loading = true
  try {
    const r = await portalApi.my()
    portal.data = r.data
    portal.loaded = true
    return portal.data
  } finally {
    portal.loading = false
  }
}

export function resetPortal() {
  portal.data = null
  portal.loaded = false
}
