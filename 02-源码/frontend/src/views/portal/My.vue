<template>
  <div>
    <div class="hero" v-if="member">
      <div class="hl">
        <el-avatar :size="52" class="av">{{ (member.nickName || '会').slice(0, 1) }}</el-avatar>
        <div>
          <div class="nm">{{ member.nickName }} <span class="lv">{{ member.levelCode }}</span></div>
          <div class="sub">{{ member.memberNo }} · {{ member.mobile }} · 注册于 {{ (member.createTime || '').slice(0, 10) }}</div>
        </div>
      </div>
      <div class="hr">
        <div><b>{{ member.pointBalance || 0 }}</b><span>积分余额</span></div>
        <div><b>¥{{ Number(member.totalAmount || 0).toFixed(2) }}</b><span>累计消费</span></div>
        <div><b>{{ member.orderCount || 0 }}</b><span>消费次数</span></div>
        <div><b>{{ member.babyCount || 0 }}</b><span>宝宝档案</span></div>
      </div>
    </div>

    <el-tabs v-model="tab" class="tabs" @tab-change="onTab">
      <el-tab-pane label="宝宝档案" name="baby">
        <div class="sec">
          <div class="card-h">我的宝宝<span class="more link" @click="openBaby()">+ 添加宝宝</span></div>
          <div class="babies">
            <div v-for="b in babies" :key="b.id" class="bc" :class="{ off: b.status !== 1 }">
              <div class="bt">
                <b>{{ b.babyName }}</b>
                <span class="tag">{{ b.monthAge }} 个月</span>
                <span class="tag g">{{ b.gender }}</span>
                <el-button link type="primary" class="ed" @click="openBaby(b)">编辑</el-button>
              </div>
              <div class="kv"><span class="k">出生日期</span><span class="v">{{ (b.birthday || '').slice(0, 10) }}</span></div>
              <div class="kv"><span class="k">身高体重</span><span class="v">{{ b.heightCm }} cm / {{ b.weightKg }} kg</span></div>
              <div class="kv"><span class="k">奶粉段位</span><span class="v">{{ b.milkStage || '—' }}　尿裤 {{ b.diaperSize || '—' }}</span></div>
              <div class="kv"><span class="k">过敏提示</span><span class="v">{{ b.allergyDesc || '无' }}</span></div>
              <div class="kv"><span class="k">状态</span><span class="v">{{ b.status === 1 ? '在看（按其月龄推荐）' : '已归档' }}</span></div>
            </div>
            <div v-if="!babies.length" class="empty">还没有宝宝档案，添加后可按月龄推荐适龄商品</div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="`我的优惠券 (${coupons.length})`" name="coupon">
        <div class="sec">
          <div class="cps">
            <div v-for="x in coupons" :key="x.receive.id" class="cp" :class="stateOf(x)">
              <div class="cl">
                <b>{{ x.coupon.couponType === '折扣' ? (10 - Number(x.coupon.discountAmount)) + '折' : '¥' + Number(x.coupon.discountAmount).toFixed(0) }}</b>
                <span>{{ x.coupon.couponType === '折扣' ? '折扣券' : '满 ' + Number(x.coupon.minAmount).toFixed(0) + ' 可用' }}</span>
              </div>
              <div class="cr">
                <div class="nm">{{ x.coupon.couponName || '优惠券' }}</div>
                <div class="sub">{{ x.receive.receiveNo }} · {{ stateText(x) }}</div>
                <div class="sub">有效期至 {{ (x.coupon.validEnd || '').slice(0, 10) }}</div>
              </div>
            </div>
            <div v-if="!coupons.length" class="empty">还没有优惠券，去商城首页领取</div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="积分中心" name="point">
        <div class="sec">
          <div class="prow">
            <div class="pcard"><span>当前积分</span><b>{{ member?.pointBalance || 0 }}</b></div>
            <div class="pcard"><span>近 20 条变动</span><b>{{ points.length }}</b></div>
            <div class="pcard"><span>积分规则</span><b class="sm">实付 1 元 = 1 分</b></div>
          </div>
          <el-table :data="points" size="small" max-height="420">
            <el-table-column prop="pointNo" label="流水号" width="150" />
            <el-table-column prop="changeType" label="类型" width="90" />
            <el-table-column label="变动" width="92" align="right">
              <template #default="{ row }">
                <b :style="{ color: row.changePoint > 0 ? 'var(--danger)' : 'var(--ok)' }">
                  {{ row.changePoint > 0 ? '+' : '' }}{{ row.changePoint }}
                </b>
              </template>
            </el-table-column>
            <el-table-column prop="afterPoint" label="余额" width="84" align="right" />
            <el-table-column prop="remark" label="说明" min-width="180" />
            <el-table-column label="时间" width="140">
              <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 16) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="复购提醒" name="remind">
        <div class="sec">
          <div class="card-h">我的复购提醒<span class="more">按预计用完日期排序</span></div>
          <el-table :data="reminders" size="small">
            <el-table-column prop="remindNo" label="提醒编号" width="140" />
            <el-table-column prop="goodsName" label="商品" min-width="180" />
            <el-table-column prop="lastBuyDate" label="上次购买" width="110" />
            <el-table-column prop="predictEndDate" label="预计用完" width="110" />
            <el-table-column prop="remindChannel" label="提醒方式" width="140" />
            <el-table-column prop="sendStatus" label="状态" width="90" />
          </el-table>
          <div class="empty" v-if="!reminders.length">暂无复购提醒，购买奶粉或纸尿裤后按用量自动生成</div>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="`我的评价 (${reviews.length})`" name="review">
        <div class="sec">
          <div class="rlist">
            <div v-for="r in reviews" :key="r.id" class="rv">
              <div class="rh"><b>{{ r.goodsName || '商品' }}</b><el-rate :model-value="r.score || 5" disabled size="small" />
                <span class="tag">{{ r.status }}</span></div>
              <div class="rc">{{ r.content }}</div>
              <div class="sub">{{ (r.createTime || '').replace('T', ' ').slice(0, 16) }}<span v-if="r.reply">　商家回复：{{ r.reply }}</span></div>
            </div>
            <div v-if="!reviews.length" class="empty">还没有评价，订单完成后可在订单列表评价</div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dlg.on" :title="dlg.form.id ? '编辑宝宝档案' : '添加宝宝档案'" width="480px">
      <el-form label-width="82px">
        <el-form-item label="宝宝昵称"><el-input v-model="dlg.form.babyName" maxlength="20" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="dlg.form.gender">
            <el-radio-button v-for="g in ['男', '女', '未知']" :key="g" :value="g">{{ g }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="dlg.form.birthday" type="date" value-format="YYYY-MM-DD" :disabled-date="afterToday" style="width:100%" />
        </el-form-item>
        <el-form-item label="身高 / 体重">
          <el-input-number v-model="dlg.form.heightCm" :min="40" :max="140" :step="0.5" />
          <span class="unit">cm</span>
          <el-input-number v-model="dlg.form.weightKg" :min="2" :max="40" :step="0.1" />
          <span class="unit">kg</span>
        </el-form-item>
        <el-form-item label="奶粉段位">
          <el-select v-model="dlg.form.milkStage" style="width:120px">
            <el-option v-for="s in ['1段', '2段', '3段', '4段', '无']" :key="s" :label="s" :value="s" />
          </el-select>
          <span class="unit">尿裤</span>
          <el-select v-model="dlg.form.diaperSize" style="width:100px">
            <el-option v-for="s in ['S', 'M', 'L', 'XL', 'XXL', '无']" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="过敏提示"><el-input v-model="dlg.form.allergyDesc" maxlength="60" placeholder="如 牛奶蛋白过敏" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg.on = false">取消</el-button>
        <el-button type="primary" :loading="dlg.saving" @click="saveBaby">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { crud } from '@/api'
import { loadPortal, portal, resetPortal } from '@/store/portal'
import { useUserStore } from '@/store/user'

const crudBaby = crud('baby-profile')
const route = useRoute()
const router = useRouter()
const store = useUserStore()

const tab = ref(route.query.tab || 'baby')
const dlg = reactive({ on: false, saving: false, form: {} })

const member = computed(() => portal.data?.member)
const babies = computed(() => portal.data?.babies || [])
const coupons = computed(() => portal.data?.coupons || [])
const points = computed(() => portal.data?.points || [])
const reminders = computed(() => portal.data?.reminders || [])
const reviews = computed(() => portal.data?.reviews || [])

function onTab(t) { router.replace({ path: '/portal/my', query: { tab: t } }) }

function stateOf(x) {
  const r = x.receive
  if (r.useStatus === '已使用') return 'used'
  if (new Date(x.coupon.validEnd).getTime() < Date.now()) return 'expired'
  return 'unused'
}
function stateText(x) {
  const s = stateOf(x)
  if (s === 'used') return '已使用'
  if (s === 'expired') return '已过期'
  return '未使用'
}
function afterToday(d) { return d.getTime() > Date.now() }

function openBaby(b) {
  dlg.form = b
    ? { ...b }
    : { memberId: store.user.id, babyName: '', gender: '女', birthday: '2025-03-01', heightCm: 75, weightKg: 9.5, milkStage: '3段', diaperSize: 'L', allergyDesc: '', status: 1 }
  dlg.on = true
}

async function saveBaby() {
  if (!dlg.form.babyName || dlg.form.babyName.trim().length < 1) { ElMessage.warning('请填写宝宝昵称'); return }
  if (!dlg.form.birthday) { ElMessage.warning('请选择出生日期'); return }
  dlg.saving = true
  try {
    const body = { ...dlg.form, babyName: dlg.form.babyName.trim(), memberId: store.user.id }
    if (body.id) await crudBaby.edit(body.id, body)
    else await crudBaby.add(body)
    ElMessage.success('已保存，月龄按出生日期自动重算')
    dlg.on = false
    resetPortal()
    await loadPortal(true)
  } finally {
    dlg.saving = false
  }
}

onMounted(() => loadPortal().catch(() => {}))
</script>

<style scoped>
.hero {
  background: #fff; border-radius: 10px; box-shadow: var(--sh); padding: 18px 20px;
  display: flex; align-items: center; gap: 20px; margin-bottom: 14px; flex-wrap: wrap;
  color: var(--text);
}
.hl { display: flex; align-items: center; gap: 14px; }
.av { background: var(--primary-2); font-size: 18px; }
.hl .nm { font-size: 16px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.hl .lv { font-size: 11px; color: var(--primary); background: #fdf2f5; border-radius: 4px; padding: 1px 6px; font-weight: 400; }
.hl .sub { font-size: 11.5px; color: var(--muted); margin-top: 3px; }
.hr { margin-left: auto; display: flex; gap: 30px; }
.hr div { text-align: right; }
.hr b { display: block; font-size: 19px; font-variant-numeric: tabular-nums; }
.hr span { font-size: 11.5px; color: var(--muted); }

.tabs :deep(.el-tabs__header) { margin-bottom: 14px; }
.sec { background: #fff; border-radius: 10px; box-shadow: var(--sh); padding: 16px 18px; }
.link { cursor: pointer; }

.babies { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 14px; }
.bc { border: 1px solid var(--line); border-radius: 10px; padding: 13px 15px; }
.bc.off { opacity: 0.66; }
.bc .bt { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.bc .bt b { font-size: 15px; }
.bc .bt .ed { margin-left: auto; }
.tag { font-size: 11px; background: #fdf2f5; color: var(--primary); border-radius: 4px; padding: 1px 6px; }
.tag.g { background: #eef2f4; color: var(--muted); }

.cps { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 12px; }
.cp { display: flex; border-radius: 10px; overflow: hidden; border: 1px solid var(--line); background: #fff; }
.cp .cl { width: 92px; flex: 0 0 92px; background: #fdf2f5; color: var(--primary); display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 12px 0; }
.cp .cl b { font-size: 21px; }
.cp .cl span { font-size: 11px; }
.cp .cr { padding: 10px 12px; }
.cp .cr .nm { font-size: 13px; font-weight: 500; }
.cp .cr .sub { font-size: 11.5px; color: var(--muted); margin-top: 2px; }
.cp.used { opacity: 0.6; }
.cp.expired { filter: grayscale(1); opacity: 0.6; }

.prow { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 14px; }
.pcard { background: #fafbfb; border: 1px solid var(--line); border-radius: 9px; padding: 12px 14px; }
.pcard span { font-size: 12px; color: var(--muted); }
.pcard b { display: block; font-size: 21px; margin-top: 2px; font-variant-numeric: tabular-nums; }
.pcard b.sm { font-size: 14px; font-weight: 500; margin-top: 6px; }

.rlist { display: grid; gap: 12px; }
.rv { border-bottom: 1px dashed var(--line); padding-bottom: 10px; }
.rv:last-child { border-bottom: none; }
.rv .rh { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.rv .rc { font-size: 13px; color: var(--text); margin: 5px 0 3px; }
.rv .sub { font-size: 11.5px; color: var(--muted); }
.empty { color: var(--muted); font-size: 13px; padding: 26px 0; text-align: center; }
.unit { font-size: 12px; color: var(--muted); margin: 0 8px 0 5px; }
:deep(.el-input-number) { width: 108px; }
@media (max-width: 980px) {
  .hr { margin-left: 0; gap: 18px; }
  .prow { grid-template-columns: 1fr; }
}
</style>
