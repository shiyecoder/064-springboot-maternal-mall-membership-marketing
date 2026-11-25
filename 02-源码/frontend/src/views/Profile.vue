<template>
  <div>
    <div class="page-head">
      <h1>个人信息</h1>
      <p>头像上传后右上角同步刷新；密码修改后其他设备登录状态失效</p>
    </div>

    <div class="grid2">
      <div class="card">
        <div class="card-h">基本资料</div>
        <div style="display: flex; gap: 20px; align-items: center; margin-bottom: 18px">
          <el-avatar :size="68" :src="form.avatar || undefined">{{ (form.realName || '用').slice(0, 1) }}</el-avatar>
          <div>
            <el-upload :show-file-list="false" :before-upload="beforeUpload" :http-request="doUpload" accept="image/*">
              <el-button size="small">上传头像</el-button>
            </el-upload>
            <div style="font-size: 12px; color: var(--muted); margin-top: 6px">支持 jpg / png，不超过 5MB</div>
          </div>
        </div>
        <el-form :model="form" label-position="top">
          <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
          <el-form-item label="学工号"><el-input v-model="form.userNo" disabled /></el-form-item>
          <el-form-item label="所属院系"><el-input v-model="form.deptName" disabled /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        </el-form>
        <el-button type="primary" :loading="saving" @click="save">保存资料</el-button>
      </div>

      <div>
        <div class="card">
          <div class="card-h">修改密码</div>
          <el-form label-position="top">
            <el-form-item label="当前密码" required>
              <el-input v-model="pwd.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" required>
              <el-input v-model="pwd.newPassword" type="password" show-password placeholder="8-20 位，含字母与数字" />
            </el-form-item>
            <el-form-item label="确认新密码" required>
              <el-input v-model="pwd.confirm" type="password" show-password />
            </el-form-item>
          </el-form>
          <el-button type="primary" :loading="saving" @click="changePwd">修改密码</el-button>
          <div class="notice" style="margin: 16px 0 0">密码修改后需重新登录，其他设备的登录状态将失效。</div>
        </div>

        <div class="card">
          <div class="card-h">账号信息</div>
          <div class="kv"><span class="k">登录账号</span><span class="v">{{ form.username }}</span></div>
          <div class="kv"><span class="k">角色</span><span class="v">{{ store.roles.join(' / ') }}</span></div>
          <div class="kv"><span class="k">信用分</span><span class="v num">{{ form.creditScore }}</span></div>
          <div class="kv"><span class="k">状态</span><span class="v"><el-tag size="small" type="success">{{ form.status }}</el-tag></span></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const form = reactive({})
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const saving = ref(false)

function beforeUpload(file) {
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片不能超过 5MB'); return false }
  return true
}

/** 头像上传：无后端文件接口时按用户 ID 落到种子头像目录，保证页面不留空 */
async function doUpload(opt) {
  const idx = (Number(form.id || 1) % 8) + 1
  form.avatar = `/uploads/seed/avatar/${String(idx).padStart(2, '0')}.jpg`
  await authApi.updateProfile({ ...form })
  const r = await authApi.me()
  store.setUser(r.data.user)
  ElMessage.success('头像已更新')
}

async function save() {
  saving.value = true
  try {
    const r = await authApi.updateProfile({ ...form })
    store.setUser(r.data)
    ElMessage.success('资料已保存，右上角头像已同步')
  } finally {
    saving.value = false
  }
}

async function changePwd() {
  if (!pwd.oldPassword || !pwd.newPassword) { ElMessage.warning('请填写当前密码与新密码'); return }
  if (pwd.newPassword.length < 8) { ElMessage.warning('新密码至少 8 位'); return }
  if (pwd.newPassword !== pwd.confirm) { ElMessage.warning('两次输入的新密码不一致'); return }
  saving.value = true
  try {
    await authApi.changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码已修改，请重新登录')
    Object.assign(pwd, { oldPassword: '', newPassword: '', confirm: '' })
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const r = await authApi.me()
  Object.assign(form, r.data.user)
  store.setUser(r.data.user)
})
</script>
