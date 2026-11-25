<template>
  <div>
    <div class="page-head">
      <h1>流失规则配置</h1>
      <p>五项权重合计必须为 100　·　发布即生成新版本，旧版本自动置为历史，历史快照不随规则变化</p>
      <div class="acts">
        <el-button :disabled="sum !== 100" @click="reset">恢复当前生效</el-button>
        <el-button type="primary" :loading="saving" @click="publish">发布新版本</el-button>
      </div>
    </div>

    <div class="two">
      <div class="card">
        <div class="card-h">因素权重<span class="more" :style="{ color: sum === 100 ? 'var(--ok)' : 'var(--danger)' }">合计 {{ sum }} / 100</span></div>
        <div v-for="f in factors" :key="f.key" class="wrow">
          <div class="wtop">
            <span class="wn">{{ f.label }}</span>
            <span class="wd">{{ f.desc }}</span>
            <span class="wv">{{ form[f.key] }}%</span>
          </div>
          <el-slider v-model="form[f.key]" :min="0" :max="60" :step="5" :show-tooltip="false" />
        </div>

        <el-divider />
        <div class="card-h" style="margin-bottom:6px">分级阈值</div>
        <el-form label-width="110px">
          <el-form-item label="高风险阈值">
            <el-input-number v-model="form.highThreshold" :min="51" :max="95" :step="1" />
            <span class="hint">≥ {{ form.highThreshold }} 判为高风险，需 48 小时内电话召回</span>
          </el-form-item>
          <el-form-item label="中风险阈值">
            <el-input-number v-model="form.midThreshold" :min="20" :max="form.highThreshold - 1" :step="1" />
            <span class="hint">{{ form.midThreshold }}-{{ form.highThreshold - 1 }} 判为中风险</span>
          </el-form-item>
          <el-form-item label="低于阈值">
            <span class="hint" style="margin-left:0">&lt; {{ form.midThreshold }} 判为低风险，仅按月推送复购提醒</span>
          </el-form-item>
          <el-form-item label="规则名称">
            <el-input v-model="form.ruleName" maxlength="40" style="width:280px" />
          </el-form-item>
          <el-form-item label="变更说明">
            <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="120" show-word-limit
              placeholder="不少于 5 个字，例如：双十一后提高间隔权重以提前识别流失" style="width:100%" />
          </el-form-item>
        </el-form>
        <el-alert v-if="sum !== 100" type="error" :closable="false" show-icon
          :title="`权重合计为 ${sum}，必须等于 100 才能发布`" />
      </div>

      <div>
        <div class="card">
          <div class="card-h">当前生效<span class="more">{{ active.ruleVersion || '-' }}</span></div>
          <div class="kv"><span class="k">规则名称</span><span class="v">{{ active.ruleName || '—' }}</span></div>
          <div class="kv"><span class="k">生效时间</span><span class="v">{{ (active.effectTime || '').replace('T', ' ').slice(0, 19) || '—' }}</span></div>
          <div class="kv"><span class="k">间隔权重</span><span class="v">{{ active.weightGap }}%　频次 {{ active.weightFreq }}%　客单 {{ active.weightAmount }}%</span></div>
          <div class="kv"><span class="k">券 / 月龄</span><span class="v">{{ active.weightCoupon }}% / {{ active.weightAge }}%</span></div>
          <div class="kv"><span class="k">阈值</span><span class="v">高 ≥{{ active.highThreshold }}　中 ≥{{ active.midThreshold }}</span></div>
          <div class="kv"><span class="k">说明</span><span class="v">{{ active.remark || '—' }}</span></div>
        </div>

        <div class="card">
          <div class="card-h">版本留痕<span class="more">共 {{ versions.length }} 版</span></div>
          <el-table :data="versions" size="small" max-height="300">
            <el-table-column prop="ruleVersion" label="版本" width="64" />
            <el-table-column prop="status" label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="row.status === '生效' ? 'success' : 'info'" size="small" effect="light">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="权重" min-width="150">
              <template #default="{ row }">
                <span class="fac">{{ row.weightGap }}/{{ row.weightFreq }}/{{ row.weightAmount }}/{{ row.weightCoupon }}/{{ row.weightAge }}</span>
              </template>
            </el-table-column>
            <el-table-column label="生效时间" width="120">
              <template #default="{ row }">{{ (row.effectTime || '').slice(0, 10) }}</template>
            </el-table-column>
          </el-table>
          <div class="mini">权重顺序：间隔 / 频次 / 客单价 / 券使用率 / 月龄。历史版本仅作留痕，不可回滚覆盖。</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { churnApi } from '@/api'

const active = ref({})
const versions = ref([])
const saving = ref(false)
const form = reactive({
  ruleName: '母婴流失风险五因素模型', weightGap: 35, weightFreq: 25, weightAmount: 20,
  weightCoupon: 10, weightAge: 10, highThreshold: 75, midThreshold: 50, remark: ''
})

const factors = [
  { key: 'weightGap', label: '距上次购买间隔', desc: '0-90 天分段线性归一，越久分越高' },
  { key: 'weightFreq', label: '近 12 月消费频次', desc: '0 次记 100，≥20 次记 0' },
  { key: 'weightAmount', label: '客单价', desc: '客单价越低流失倾向越高' },
  { key: 'weightCoupon', label: '券使用率', desc: '只领不用视为价格敏感且低黏性' },
  { key: 'weightAge', label: '会员月龄', desc: '注册 12 个月以上且无复购加权' }
]

const sum = computed(() => form.weightGap + form.weightFreq + form.weightAmount + form.weightCoupon + form.weightAge)

function fill(r) {
  if (!r) return
  Object.keys(form).forEach((k) => { if (r[k] !== undefined && r[k] !== null) form[k] = r[k] })
  form.weightGap = r.weightGap ?? 35
  form.weightFreq = r.weightFreq ?? 25
  form.weightAmount = r.weightAmount ?? 20
  form.weightCoupon = r.weightCoupon ?? 10
  form.weightAge = r.weightAge ?? 10
  form.highThreshold = r.highThreshold ?? 75
  form.midThreshold = r.midThreshold ?? 50
  form.remark = ''
}

async function load() {
  const r = await churnApi.rule()
  active.value = r.data.active || {}
  versions.value = r.data.versions || []
  fill(r.data.active)
}

function reset() { fill(active.value); ElMessage.info('已恢复为当前生效规则') }

async function publish() {
  if (sum.value !== 100) { ElMessage.warning(`权重合计必须等于 100，当前 ${sum.value}`); return }
  if (!form.remark || form.remark.trim().length < 5) { ElMessage.warning('变更说明不少于 5 个字'); return }
  saving.value = true
  try {
    const r = await churnApi.saveRule({ ...form, remark: form.remark.trim() })
    ElMessage.success(`已发布 ${r.data.ruleVersion}，旧版本转为历史`)
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.two { display: grid; grid-template-columns: minmax(0, 1fr) 380px; gap: 16px; align-items: start; }
.wrow { margin-bottom: 6px; }
.wtop { display: flex; align-items: baseline; gap: 8px; font-size: 13px; margin-bottom: -4px; }
.wtop .wn { font-weight: 500; }
.wtop .wd { font-size: 11.5px; color: var(--muted); }
.wtop .wv { margin-left: auto; font-variant-numeric: tabular-nums; color: var(--primary); font-weight: 600; }
.hint { font-size: 11.5px; color: var(--muted); margin-left: 10px; }
.mini { font-size: 11.5px; color: var(--muted); margin-top: 10px; line-height: 1.6; }
.fac { font-size: 11.5px; color: var(--muted); font-variant-numeric: tabular-nums; }
:deep(.el-slider) { --el-slider-height: 4px; }
:deep(.el-slider__button) { width: 13px; height: 13px; }
</style>
