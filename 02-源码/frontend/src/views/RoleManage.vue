<template>
  <div>
    <div class="page-head">
      <h1>角色与数据范围</h1>
      <p>权限按「菜单权限 + 按钮权限 + 数据范围」三层控制，前端隐藏按钮不等于授权</p>
      <div class="acts"><el-button type="primary">+ 新增角色</el-button></div>
    </div>

    <div class="card">
      <el-table :data="roles" size="default">
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column label="数据范围" width="120">
          <template #default="{ row }">
            <el-tag :type="row.dataScope === 3 ? 'success' : row.dataScope === 2 ? 'warning' : 'info'" size="small">
              {{ scopeText(row.dataScope) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="职责说明" min-width="280" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="row.status === '正常' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openAssign(row)">分配菜单</el-button>
            <el-button link type="primary" size="small">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="dlg" :title="`为「${cur?.roleName || ''}」分配菜单`" size="520px">
      <div class="drawer-sub">勾选后保存将整体替换该角色的菜单授权；「菜单管理」项对非超管不可见，避免越权修改权限树。</div>
      <el-tree
        ref="treeRef"
        :data="tree"
        show-checkbox
        node-key="id"
        :props="{ label: 'menuName', children: 'children' }"
        :default-checked-keys="checked"
        default-expand-all
        style="max-height: 520px; overflow: auto"
      />
      <div style="display: flex; gap: 8px; margin-top: 16px">
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" style="margin-left: auto" @click="save">保存授权</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { crud, roleApi, menuApi } from '@/api'

const roleApiCrud = crud('role')
const roles = ref([])
const tree = ref([])
const dlg = ref(false)
const cur = ref(null)
const checked = ref([])
const treeRef = ref()

const scopeText = (s) => ({ 1: '本人', 2: '本部门', 3: '全部' }[s] || '本人')

/** 扁平菜单 → 树 */
function toTree(list) {
  const map = {}
  list.forEach((m) => { map[m.id] = { ...m, children: [] } })
  const roots = []
  list.forEach((m) => {
    const node = map[m.id]
    if (m.parentId && map[m.parentId]) map[m.parentId].children.push(node)
    else roots.push(node)
  })
  return roots
}

async function openAssign(row) {
  cur.value = row
  const [m, c] = await Promise.all([menuApi.tree(), roleApi.menus(row.id)])
  tree.value = toTree(m.data || [])
  checked.value = c.data || []
  dlg.value = true
}

async function save() {
  const ids = [...treeRef.value.getCheckedKeys(), ...treeRef.value.getHalfCheckedKeys()]
  await roleApi.saveMenus(cur.value.id, ids)
  ElMessage.success('授权已保存')
  dlg.value = false
}

onMounted(async () => {
  const r = await roleApiCrud.page({ page: 1, size: 20 })
  roles.value = r.data.records || []
})
</script>
