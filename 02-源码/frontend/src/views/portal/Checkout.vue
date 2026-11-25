<template>
  <div class="wrap">
    <div class="card">
      <div class="card-h">确认订单<span class="more">{{ cart.items.length }} 种商品 · 共 {{ cartCount }} 件</span></div>
      <el-empty v-if="!cart.items.length" description="购物车是空的">
        <el-button type="primary" @click="$router.push('/portal/mall')">去商城选购</el-button>
      </el-empty>
      <div v-for="i in cart.items" :key="i.goodsId" class="line">
        <img :src="i.coverImage" :alt="i.goodsName" />
        <div class="li">
          <div class="nm">{{ i.goodsName }}</div>
          <div class="sub">{{ i.specDesc }}</div>
        </div>
        <el-input-number v-model="i.qty" :min="1" :max="Math.min(i.stockQty || 20, 20)" size="small"
          @change="(v) => setQty(i.goodsId, v)" />
        <div class="pr">¥{{ (i.price * i.qty).toFixed(2) }}</div>
        <el-button link type="danger" @click="removeItem(i.goodsId)">移除</el-button>
      </div>

      <el-divider />
      <div class="card-h" style="margin:0 0 10px">收货信息</div>
      <el-form :model="addr" label-width="80px" class="addr">
        <el-form-item label="收货人"><el-input v-model="addr.receiverName" maxlength="20" style="width:200px" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addr.receiverMobile" maxlength="11" style="width:200px" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="addr.receiverAddress" maxlength="120" placeholder="杭州市滨江区 …" /></el-form-item>
      </el-form>
    </div>

    <div class="side">
      <div class="card">
        <div class="card-h">优惠券</div>
        <el-select v-model="couponReceiveId" placeholder="不使用优惠券" clearable style="width:100%" @change="compute">
          <el-option v-for="c in usableCoupons" :key="c.receive.id" :value="c.receive.id"
            :label="`${c.coupon.couponName}（满 ${Number(c.coupon.minAmount).toFixed(0)} 减 ${Number(c.coupon.discountAmount).toFixed(0)}）`" />
        </el-select>
        <div class="mini" v-if="!usableCoupons.length">暂无可用券，去商城首页领取</div>

        <el-divider />
        <div class="card-h" style="margin:0 0 8px">支付方式</div>
        <el-radio-group v-model="payType" size="small">
          <el-radio-button v-for="p in ['微信', '支付宝', '会员储值']" :key="p" :value="p">{{ p }}</el-radio-button>
        </el-radio-group>

        <el-divider />
        <div class="sum">
          <div><span>商品金额</span><b>¥{{ goodsAmount.toFixed(2) }}</b></div>
          <div><span>优惠券</span><b class="cut">-¥{{ discount.toFixed(2) }}</b></div>
          <div><span>运费</span><b>{{ freight.toFixed(2) === '0.00' ? '免运费' : '¥' + freight.toFixed(2) }}</b></div>
          <div class="total"><span>应付</span><b>¥{{ payAmount.toFixed(2) }}</b></div>
        </div>
        <div class="tip">满 199 免运费；券门槛按商品金额计算，运费不计入门槛。</div>
        <el-button type="primary" class="go" :loading="submitting" :disabled="!cart.items.length" @click="submit">
          提交订单
        </el-button>
      </div>
      <div class="card">
        <div class="card-h">会员权益</div>
        <div class="kv"><span class="k">当前等级</span><span class="v">{{ member?.levelCode }}</span></div>
        <div class="kv"><span class="k">积分余额</span><span class="v">{{ member?.pointBalance || 0 }}</span></div>
        <div class="kv"><span class="k">本单预计积分</span><span class="v">{{ Math.floor(payAmount) }}</span></div>
        <div class="mini">订单完成后积分入账，退款订单按规则扣回。</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, portalApi } from '@/api'
import { cart, cartCount, cartAmount, clearCart, removeItem, setQty } from '@/store/cart'
import { loadPortal, portal } from '@/store/portal'
import { useUserStore } from '@/store/user'

const router = useRouter()
const store = useUserStore()
const submitting = ref(false)
const couponReceiveId = ref(null)
const payType = ref('微信')
const myCoupons = ref([])
const addr = reactive({ receiverName: '', receiverMobile: '', receiverAddress: '杭州市滨江区江南大道 588 号 3 幢 1201' })

const member = computed(() => portal.data?.member || {})
const goodsAmount = computed(() => cartAmount.value)

const usableCoupons = computed(() => {
  return myCoupons.value.filter((x) => {
    if (x.receive.useStatus !== '未使用') return false
    return goodsAmount.value >= Number(x.coupon.minAmount || 0)
  })
})

const selectedCoupon = computed(() => usableCoupons.value.find((x) => x.receive.id === couponReceiveId.value)?.coupon)

const discount = computed(() => {
  const c = selectedCoupon.value
  if (!c) return 0
  const amount = Number(c.discountAmount || 0)
  if (c.couponType === '满减' || c.couponType === '立减') return Math.min(amount, goodsAmount.value)
  if (c.couponType === '折扣') return Math.min(goodsAmount.value * (1 - amount / 10), goodsAmount.value)
  return 0
})

const freight = computed(() => {
  const after = goodsAmount.value - discount.value
  if (selectedCoupon.value?.couponType === '免邮') return 0
  return after >= 199 ? 0 : 8
})
const payAmount = computed(() => Math.max(0, goodsAmount.value - discount.value + freight.value))

function compute() { /* 依赖 computed 自动重算，保留钩子供 el-select change 触发 */ }

async function loadCoupons() {
  const r = await portalApi.coupons()
  // 券活动详情复用「我的中心」已缓存的 coupon 对象，避免逐条请求
  const cache = new Map()
  ;(portal.data?.coupons || []).forEach((x) => cache.set(x.coupon.id, x.coupon))
  myCoupons.value = (r.data || []).map((receive) => ({
    receive,
    coupon: cache.get(receive.couponId) || { couponName: receive.couponName || '优惠券', minAmount: 0, discountAmount: 0, couponType: '满减' }
  }))
}

async function submit() {
  if (!addr.receiverName.trim()) { ElMessage.warning('请填写收货人'); return }
  if (!/^1\d{10}$/.test(addr.receiverMobile)) { ElMessage.warning('请填写 11 位手机号'); return }
  if (!addr.receiverAddress.trim()) { ElMessage.warning('请填写收货地址'); return }
  submitting.value = true
  try {
    const r = await orderApi.create({
      memberId: store.user.id,
      items: cart.items.map((i) => ({ goodsId: i.goodsId, qty: i.qty })),
      couponReceiveId: couponReceiveId.value || null,
      payType: payType.value,
      receiverName: addr.receiverName.trim(),
      receiverMobile: addr.receiverMobile,
      receiverAddress: addr.receiverAddress.trim()
    })
    const order = r.data
    clearCart()
    const ok = await ElMessageBox.confirm(
      `订单号 ${order.orderNo}　应付 ¥${Number(order.payAmount).toFixed(2)}`, '下单成功，是否立即支付？',
      { confirmButtonText: '立即支付', cancelButtonText: '稍后支付', type: 'success' }
    ).catch(() => false)
    if (ok) {
      await orderApi.pay(order.id, payType.value)
      ElMessage.success('支付成功，积分已入账')
    }
    await loadPortal(true).catch(() => {})
    router.push('/portal/orders')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadPortal().catch(() => {})
  await loadCoupons()
  addr.receiverName = portal.data?.member?.nickName || store.user?.realName || ''
  addr.receiverMobile = portal.data?.member?.mobile || ''
})
</script>

<style scoped>
.wrap { display: grid; grid-template-columns: minmax(0, 1fr) 330px; gap: 16px; align-items: start; }
.side { display: grid; gap: 16px; }
.line { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f4f5f6; }
.line img { width: 60px; height: 60px; border-radius: 8px; object-fit: cover; background: #f3f5f6; }
.line .li { flex: 1; min-width: 0; }
.line .nm { font-size: 13px; font-weight: 500; }
.line .sub { font-size: 11.5px; color: var(--muted); margin-top: 2px; }
.line .pr { width: 88px; text-align: right; font-variant-numeric: tabular-nums; color: var(--danger); font-weight: 600; }
.addr :deep(.el-form-item) { margin-bottom: 12px; }
.sum > div { display: flex; justify-content: space-between; font-size: 13px; padding: 4px 0; color: var(--muted); }
.sum b { color: var(--text); font-variant-numeric: tabular-nums; }
.sum .cut { color: var(--ok); }
.sum .total { border-top: 1px dashed var(--line); margin-top: 6px; padding-top: 10px; font-size: 14px; }
.sum .total b { font-size: 21px; color: var(--danger); }
.tip { font-size: 11.5px; color: var(--muted); margin: 8px 0 12px; line-height: 1.6; }
.go { width: 100%; height: 42px; font-size: 15px; }
.mini { font-size: 11.5px; color: var(--muted); margin-top: 8px; }
@media (max-width: 980px) { .wrap { grid-template-columns: 1fr; } }
</style>
