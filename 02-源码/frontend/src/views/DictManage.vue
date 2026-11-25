<template>
  <div>
    <div class="page-head">
      <h1>字典管理</h1>
      <p>状态、类型、级别等枚举在此统一维护，页面下拉一律从字典接口取值，禁止前后端各自硬编码</p>
    </div>

    <div class="grid2">
      <div class="card">
        <div class="card-h">字典类型</div>
        <el-table :data="types" size="small" highlight-current-row @row-click="pick">
          <el-table-column prop="dictName" label="类型名称" width="150" />
          <el-table-column prop="dictType" label="类型编码" min-width="150" />
          <el-table-column label="状态" width="86">
            <template #default="{ row }"><el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag></template>
          </el-table-column>
        </el-table>
      </div>
      <div class="card">
        <div class="card-h">字典项 · {{ curType?.dictName || '请选择左侧类型' }}</div>
        <el-table :data="items" size="small">
          <el-table-column prop="dictLabel" label="标签" width="130" />
          <el-table-column prop="dictValue" label="值" min-width="140" />
          <el-table-column label="样式" width="96">
            <template #default="{ row }"><el-tag :type="tagType(row.tagType)" size="small">{{ row.tagType || 'info' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="sortNo" label="排序" width="76" />
          <el-table-column label="状态" width="86">
            <template #default="{ row }">{{ row.status }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { dictApi } from '@/api'
import { tagType } from '@/utils/format'

const types = ref([])
const items = ref([])
const curType = ref(null)
const all = ref({})

async function pick(row) {
  curType.value = row
  items.value = all.value[row.dictType] || []
}

onMounted(async () => {
  const [t, a] = await Promise.all([dictApi.types(), dictApi.all()])
  types.value = t.data || []
  all.value = a.data || {}
  if (types.value.length) pick(types.value[0])
})
</script>
