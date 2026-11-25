import { reactive, computed } from 'vue'

/**
 * 购物车：门户内跨页共享（商城 → 结算），存 sessionStorage 防止刷新丢失。
 * 只在会员本人浏览器内使用，不做服务端购物车，避免与订单库存校验产生两套口径。
 */
const KEY = 'mom_cart'
const saved = (() => {
  try { return JSON.parse(sessionStorage.getItem(KEY) || '[]') } catch { return [] }
})()

export const cart = reactive({ items: saved })

function persist() {
  sessionStorage.setItem(KEY, JSON.stringify(cart.items))
}

export function addToCart(goods, qty = 1) {
  const hit = cart.items.find((i) => i.goodsId === goods.id)
  const price = Number(goods.memberPrice ?? goods.salePrice ?? 0)
  if (hit) {
    hit.qty = Math.min(hit.qty + qty, Math.min(goods.stockQty || 20, 20))
  } else {
    cart.items.push({
      goodsId: goods.id,
      goodsName: goods.goodsName,
      specDesc: goods.specDesc,
      coverImage: goods.coverImage,
      price,
      salePrice: Number(goods.salePrice || price),
      qty: Math.min(qty, Math.min(goods.stockQty || 20, 20)),
      stockQty: goods.stockQty
    })
  }
  persist()
}

export function setQty(goodsId, qty) {
  const hit = cart.items.find((i) => i.goodsId === goodsId)
  if (hit) hit.qty = Math.max(1, Math.min(qty, 20))
  persist()
}

export function removeItem(goodsId) {
  const i = cart.items.findIndex((x) => x.goodsId === goodsId)
  if (i >= 0) cart.items.splice(i, 1)
  persist()
}

export function clearCart() {
  cart.items.splice(0, cart.items.length)
  persist()
}

export const cartCount = computed(() => cart.items.reduce((s, i) => s + i.qty, 0))
export const cartAmount = computed(() => cart.items.reduce((s, i) => s + i.price * i.qty, 0))
