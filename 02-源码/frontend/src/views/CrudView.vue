<template>
  <div>
    <div class="page-head">
      <h1>{{ cfg.name }}</h1>
      <p v-if="total">共 {{ total }} 条记录</p>
      <div class="acts">
        <el-button v-if="cfg.actions && hasAnyAction" @click="reload">刷新</el-button>
        <el-button v-if="!cfg.readonly" type="primary" @click="openAdd">+ {{ cfg.addText || '新增' }}</el-button>
      </div>
    </div>

    <div class="filters">
      <div class="fi" v-for="f in cfg.filters || []" :key="f.prop">
        <span>{{ f.label }}</span>
        <el-select v-if="f.dict" v-model="query[f.col || f.prop]" clearable placeholder="全部" @change="reload">
          <el-option v-for="d in dict[f.dict] || []" :key="d.id" :label="d.dictLabel" :value="d.dictValue" />
        </el-select>
        <el-select v-else-if="f.options" v-model="query[f.col || f.prop]" clearable placeholder="全部" @change="reload">
          <el-option v-for="o in f.options" :key="o" :label="o" :value="o" />
        </el-select>
        <el-input v-else v-model="query[f.col || f.prop]" clearable placeholder="请输入" style="width: 150px" @keyup.enter="reload" />
      </div>
      <el-input
        v-model="query.keyword"
        :placeholder="cfg.keyword || '搜索'"
        clearable
        style="width: 220px"
        @keyup.enter="reload"
        @clear="reload"
      />
      <el-button type="primary" plain @click="reload">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="spacer"></div>
    </div>

    <div class="card" style="padding: 0">
      <el-table :data="rows" v-loading="loading" stripe style="width: 100%" :default-sort="{ prop: 'id', order: 'descending' }">
        <el-table-column
          v-for="c in cfg.columns"
          :key="c.prop"
          :label="c.label"
          :prop="c.prop"
          :width="c.width"
          :min-width="c.minWidth"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <!-- 状态标签：颜色只是辅助，必须同时显示中文 -->
            <el-tag v-if="c.type === 'tag'" :type="tagType(row[c.prop])" size="small" effect="light">
              {{ row[c.prop] || '-' }}
            </el-tag>
            <span v-else-if="c.type === 'datetime'">{{ formatDateTime(row[c.prop]) }}</span>
            <span v-else-if="c.type === 'date'">{{ formatDate(row[c.prop]) }}</span>
            <span v-else-if="c.type === 'money'" class="num">¥{{ money(row[c.prop]) }}</span>
            <span v-else-if="c.type === 'dim'" class="muted">{{ row[c.prop] || '-' }}</span>
            <span v-else-if="c.type === 'range'">{{ formatRange(row[c.prop], row[c.endProp]) }}</span>
            <span v-else-if="c.type === 'pass'">
              <el-tag :type="row.passFlag === 1 ? 'success' : 'danger'" size="small">
                {{ row.passFlag === 1 ? '通过' : '未通过' }}
              </el-tag>
            </span>
            <!-- 图片：真实本地文件，走 /uploads 静态映射 -->
            <div v-else-if="c.type === 'image'" class="imgs">
              <el-image
                v-for="(u, i) in splitImgs(row[c.prop])"
                :key="i"
                :src="u"
                :preview-src-list="splitImgs(row[c.prop])"
                fit="cover"
                class="thumb"
                preview-teleported
              />
              <span v-if="!splitImgs(row[c.prop]).length" style="color: var(--muted)">-</span>
            </div>
            <el-avatar v-else-if="c.type === 'avatar'" :size="30" :src="row.avatar || undefined">
              {{ (row.realName || '用').slice(0, 1) }}
            </el-avatar>
            <!-- 库存进度条：低于最低库存标红 -->
            <div v-else-if="c.type === 'progress'" class="stock">
              <span class="num" :class="{ low: isLow(row, c) }">{{ row[c.prop] }}</span>
              <div class="bar"><i :style="{ width: pct(row, c) + '%', background: isLow(row, c) ? 'var(--danger)' : 'var(--primary-2)' }"></i></div>
            </div>
            <span v-else class="num">{{ row[c.prop] ?? '-' }}</span>
          </template>
        </el-table-column>

        <!-- 操作列：详情 + 1 个高频动作外显，其余收进「更多」，避免挤压数据列 -->
        <el-table-column label="操作" :width="opWidth" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="primaryAction(row)"
              link
              :type="primaryAction(row).danger ? 'danger' : 'primary'"
              size="small"
              @click="runAction(primaryAction(row), row)"
            >{{ primaryAction(row).label }}</el-button>
            <el-dropdown v-if="hasMore(row)" trigger="click" @command="(c) => onCommand(c, row)">
              <el-button link type="primary" size="small">更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="a in moreActions(row)" :key="a.key" :command="'act:' + a.key">
                    {{ a.label }}
                  </el-dropdown-item>
                  <el-dropdown-item v-if="!cfg.readonly" command="edit" :divided="moreActions(row).length > 0">编辑</el-dropdown-item>
                  <el-dropdown-item v-if="!cfg.readonly" command="del">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; padding: 14px 16px">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="reload"
          @current-change="load"
        />
      </div>
    </div>

    <!-- 新增 / 编辑抽屉 -->
    <el-drawer v-model="drawer" :title="editing ? '编辑' + cfg.name : cfg.addText || '新增'" size="480px">
      <div class="drawer-sub">带 * 为必填项。编号类字段由后端生成时，此处可留空。</div>
      <el-form :model="form" :rules="formRules" ref="formRef" label-position="top">
        <el-form-item
          v-for="f in cfg.form || []"
          :key="f.prop"
          :label="f.label"
          :prop="f.prop"
          :class="{ full: f.full }"
        >
          <el-select v-if="f.type === 'dict'" v-model="form[f.prop]" clearable style="width: 100%">
            <el-option v-for="d in dict[f.dict] || []" :key="d.id" :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
          <el-select v-else-if="f.type === 'select'" v-model="form[f.prop]" clearable style="width: 100%">
            <el-option
              v-for="o in f.options"
              :key="typeof o === 'object' ? o.value : o"
              :label="typeof o === 'object' ? o.label : o"
              :value="typeof o === 'object' ? o.value : o"
            />
          </el-select>
          <el-input-number v-else-if="f.type === 'number'" v-model="form[f.prop]" :controls="false" style="width: 100%" />
          <el-input v-else-if="f.type === 'textarea'" v-model="form[f.prop]" type="textarea" :rows="3" />
          <el-date-picker
            v-else-if="f.type === 'date'"
            v-model="form[f.prop]"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
          <el-date-picker
            v-else-if="f.type === 'datetime'"
            v-model="form[f.prop]"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
          <el-input v-else v-model="form[f.prop]" :placeholder="f.tip || ''" />
        </el-form-item>
      </el-form>
      <div style="display: flex; gap: 8px; margin-top: 8px">
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" :loading="saving" style="margin-left: auto" @click="save">保存</el-button>
      </div>
    </el-drawer>

    <!-- 业务动作对话框 -->
    <el-dialog v-model="actDlg" :title="actCfg?.label" width="460px">
      <div v-if="actCfg?.danger" class="notice warn" style="margin-bottom: 14px">
        该操作会写入业务流水并通知相关人员，请确认影响范围后再提交。
      </div>
      <el-form :model="actForm" label-position="top">
        <el-form-item
          v-for="f in actCfg?.dialog || []"
          :key="f.prop"
          :label="f.label"
          :required="f.required"
        >
          <el-select v-if="f.type === 'dict'" v-model="actForm[f.prop]" style="width: 100%">
            <el-option v-for="d in dict[f.dict] || []" :key="d.id" :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
          <el-select v-else-if="f.type === 'select'" v-model="actForm[f.prop]" style="width: 100%">
            <el-option
              v-for="o in f.options"
              :key="typeof o === 'object' ? o.value : o"
              :label="typeof o === 'object' ? o.label : o"
              :value="typeof o === 'object' ? o.value : o"
            />
          </el-select>
          <el-input-number v-else-if="f.type === 'number'" v-model="actForm[f.prop]" :controls="false" style="width: 100%" />
          <el-input v-else-if="f.type === 'textarea'" v-model="actForm[f.prop]" type="textarea" :rows="3" />
          <el-input v-else v-model="actForm[f.prop]" :placeholder="f.tip || ''" />
          <div v-if="f.tip" style="font-size: 11.5px; color: var(--muted)">{{ f.tip }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actDlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAction">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDlg" :title="cfg.name + '详情'" size="460px">
      <div class="drawer-sub">{{ detailTitle }}</div>
      <div v-for="c in cfg.columns" :key="c.prop" class="kv">
        <span class="k">{{ c.label }}</span>
        <span class="v">
          <el-tag v-if="c.type === 'tag'" :type="tagType(detail[c.prop])" size="small">{{ detail[c.prop] || '-' }}</el-tag>
          <span v-else-if="c.type === 'datetime'">{{ formatDateTime(detail[c.prop]) }}</span>
          <span v-else-if="c.type === 'date'">{{ formatDate(detail[c.prop]) }}</span>
          <span v-else-if="c.type === 'range'">{{ formatRange(detail[c.prop], detail[c.endProp]) }}</span>
          <span v-else>{{ detail[c.prop] ?? '-' }}</span>
        </span>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ENTITIES } from '@/config/entities'
import { crud, dictApi, actionApi } from '@/api'
import { formatDate, formatDateTime, formatRange, money, tagType } from '@/utils/format'

const route = useRoute()
const cfg = computed(() => ENTITIES[route.meta.entity] || ENTITIES.question)
const api = computed(() => crud(cfg.value.api))

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dict = ref({})
const query = reactive({ page: 1, size: 10, keyword: '' })

const drawer = ref(false)
const editing = ref(false)
const formRef = ref()
const form = reactive({})
const detailDlg = ref(false)
const detail = ref({})
const detailTitle = computed(() => detail.value?.id ? `主键 #${detail.value.id}` : '')

const actDlg = ref(false)
const actCfg = ref(null)
const actForm = reactive({})
const actRow = ref(null)

const formRules = computed(() => {
  const r = {}
  for (const f of cfg.value.form || []) {
    if (f.required) r[f.prop] = [{ required: true, message: `请填写${f.label}`, trigger: 'blur' }]
  }
  return r
})

const hasAnyAction = computed(() => (cfg.value.actions || []).length > 0)
const opWidth = computed(() => 132)
/** 当前行第一个可执行动作，外显为主动作 */
function visibleActions(row) {
  return (cfg.value.actions || []).filter((a) => !a.show || a.show(row))
}
function primaryAction(row) {
  return visibleActions(row)[0] || null
}
function moreActions(row) {
  return visibleActions(row).slice(1)
}
function hasMore(row) {
  return moreActions(row).length > 0 || !cfg.value.readonly
}
function onCommand(cmd, row) {
  if (cmd === 'edit') return openEdit(row)
  if (cmd === 'del') return confirmRemove(row)
  const a = visibleActions(row).find((x) => 'act:' + x.key === cmd)
  if (a) runAction(a, row)
}
async function confirmRemove(row) {
  try {
    await ElMessageBox.confirm('删除后列表将不再显示，确认删除该记录？', '删除确认', {
      type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消'
    })
    await remove(row)
  } catch (e) {
    /* 用户取消 */
  }
}


function splitImgs(v) {
  if (!v) return []
  return String(v).split(',').map((s) => s.trim()).filter(Boolean)
}
function isLow(row, c) {
  return c.minProp && Number(row[c.prop]) < Number(row[c.minProp] || 0)
}
function pct(row, c) {
  const min = Number(row[c.minProp] || 1)
  return Math.min(100, Math.round((Number(row[c.prop]) / Math.max(min * 2, 1)) * 100))
}

async function loadDict() {
  try {
    const r = await dictApi.all()
    dict.value = r.data || {}
  } catch (e) {
    dict.value = {}
  }
}

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    for (const f of cfg.value.filters || []) {
      const key = f.col || f.prop
      if (query[key]) params[key] = query[key]
    }
    const r = await api.value.page(params)
    rows.value = r.data.records || []
    total.value = r.data.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  query.page = 1
  load()
}

function reset() {
  query.keyword = ''
  for (const f of cfg.value.filters || []) query[f.col || f.prop] = ''
  reload()
}

function clearForm() {
  for (const k of Object.keys(form)) delete form[k]
}

function openAdd() {
  editing.value = false
  clearForm()
  drawer.value = true
}

function openEdit(row) {
  editing.value = true
  clearForm()
  Object.assign(form, JSON.parse(JSON.stringify(row)))
  drawer.value = true
}

async function openDetail(row) {
  const r = await api.value.detail(row.id)
  detail.value = r.data
  detailDlg.value = true
}

async function save() {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  saving.value = true
  try {
    if (editing.value) {
      await api.value.edit(form.id, form)
      ElMessage.success('已保存')
    } else {
      await api.value.add(form)
      ElMessage.success('已新增')
    }
    drawer.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await api.value.remove(row.id)
  ElMessage.success('已删除')
  load()
}

function runAction(a, row) {
  actCfg.value = a
  actRow.value = row
  for (const k of Object.keys(actForm)) delete actForm[k]
  if (a.dialog) for (const f of a.dialog) actForm[f.prop] = f.type === 'number' ? 0 : undefined
  if (a.dialog) {
    actDlg.value = true
  } else {
    // 无表单时用二次确认
    ElMessageBoxConfirm(a, row)
  }
}

function ElMessageBoxConfirm(a, row) {
  const fn = a.api
  if (!a.confirm) {
    submitDirect(a, row, {})
    return
  }
  import('element-plus').then(({ ElMessageBox }) => {
    ElMessageBox.confirm(a.confirm, a.label, { type: a.danger ? 'warning' : 'info' })
      .then(() => submitDirect(a, row, {}))
      .catch(() => {})
  })
}

async function submitDirect(a, row, extra) {
  saving.value = true
  try {
    await actionApi[a.api](row.id, { ...extra })
    ElMessage.success(`${a.label}成功`)
    load()
  } finally {
    saving.value = false
  }
}

async function submitAction() {
  const a = actCfg.value
  if (a.dialog) {
    for (const f of a.dialog) {
      if (f.required && (actForm[f.prop] === undefined || actForm[f.prop] === '' || actForm[f.prop] === null)) {
        ElMessage.warning(`请填写${f.label}`)
        return
      }
    }
  }
  saving.value = true
  try {
    await actionApi[a.api](actRow.value.id, { ...(a.extra || {}), ...actForm })
    ElMessage.success(`${a.label}成功`)
    actDlg.value = false
    load()
  } finally {
    saving.value = false
  }
}

watch(() => route.meta.entity, () => {
  clearForm()
  Object.assign(query, { page: 1, keyword: '' })
  load()
})

onMounted(async () => {
  await loadDict()
  await load()
})
</script>

<style scoped>
.imgs { display: flex; gap: 4px; }
.thumb { width: 34px; height: 26px; border-radius: 4px; }
.stock { display: flex; align-items: center; gap: 7px; }
.stock .num { width: 40px; font-variant-numeric: tabular-nums; }
.stock .num.low { color: var(--danger); font-weight: 600; }
.stock .bar { flex: 1; min-width: 46px; height: 6px; background: #eef0f1; border-radius: 3px; overflow: hidden; }
.stock .bar i { display: block; height: 100%; }
:deep(.el-form-item.full) { grid-column: 1 / -1; }
</style>
