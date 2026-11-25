<template>
  <div>
    <div class="page-head">
      <h1>菜单管理</h1>
      <p>目录 / 菜单 / 按钮三级权限；「菜单管理」及其分配入口对非超级管理员隐藏</p>
      <div class="acts"><el-button type="primary" @click="openAdd(null)">+ 新增根目录</el-button></div>
    </div>

    <div class="notice">按钮级权限（type=F）只控制前端显隐，所有写接口在后端按数据范围复核，避免绕过。</div>

    <div class="card">
      <el-table :data="tree" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="菜单名称" min-width="220" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 'M' ? 'info' : row.type === 'C' ? 'success' : 'info'" size="small">
              {{ row.type === 'M' ? '目录' : row.type === 'C' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由" width="200" />
        <el-table-column prop="perms" label="权限标识" width="180" />
        <el-table-column prop="sortNo" label="排序" width="76" />
        <el-table-column label="可见" width="76">
          <template #default="{ row }">{{ row.visible === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openAdd(row)">新增子项</el-button>
            <el-popconfirm title="确认删除该菜单？" @confirm="remove(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dlg" :title="form.id ? '编辑菜单' : '新增菜单'" width="480px">
      <el-form :model="form" label-position="top">
        <el-form-item label="父级"><el-input v-model="form.parentName" disabled /></el-form-item>
        <el-form-item label="菜单名称" required><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="目录" value="M" /><el-option label="菜单" value="C" /><el-option label="按钮" value="F" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.perms" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortNo" :controls="false" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { menuApi } from '@/api'

const tree = ref([])
const dlg = ref(false)
const form = reactive({ id: null, parentId: 0, parentName: '根目录', menuName: '', type: 'C', path: '', perms: '', sortNo: 1, visible: 1 })

function toTree(list) {
  const map = {}
  list.forEach((m) => { map[m.id] = { ...m, children: [] } })
  const roots = []
  list.forEach((m) => {
    if (m.parentId && map[m.parentId]) map[m.parentId].children.push(map[m.id])
    else roots.push(map[m.id])
  })
  return roots
}

async function load() {
  const r = await menuApi.tree()
  tree.value = toTree(r.data || [])
}

function openAdd(row) {
  Object.assign(form, {
    id: null,
    parentId: row ? row.id : 0,
    parentName: row ? row.menuName : '根目录',
    menuName: '', type: row ? 'C' : 'M', path: '', perms: '', sortNo: 1, visible: 1
  })
  dlg.value = true
}

async function save() {
  if (!form.menuName) { ElMessage.warning('请填写菜单名称'); return }
  await menuApi.add({ parentId: form.parentId, menuName: form.menuName, type: form.type, path: form.path, perms: form.perms, sortNo: form.sortNo, visible: form.visible, status: '正常' })
  ElMessage.success('已保存')
  dlg.value = false
  load()
}

async function remove(row) {
  await menuApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
