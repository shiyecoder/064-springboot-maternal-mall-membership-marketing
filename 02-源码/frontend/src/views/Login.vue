<template>
  <div class="login">
    <div class="left">
      <div class="brand">
        <span class="logo">芽</span>
        <div>芽芽母婴<small>MOM &amp; BABY CRM</small></div>
      </div>
      <div class="copy">
        <h1>把宝宝的月龄<br />变成门店的生意</h1>
        <p>适龄商品推荐、宝宝档案、优惠券核销与会员流失预警，一套系统管住门店会员的每一次复购。</p>
        <div class="kpis">
          <div><b>适龄</b><i>按宝宝月龄推荐商品</i></div>
          <div><b>分层</b><i>五因素流失风险评分</i></div>
          <div><b>复购</b><i>按用量预测下次购买</i></div>
        </div>
      </div>
      <div class="tip">门店会员经营平台 · 不接真实支付与短信通道</div>
    </div>

    <div class="right">
      <div class="form">
        <h2>欢迎回来</h2>
        <p class="sub">请使用门店账号登录，会员可选择「会员家长」进入商城</p>
        <el-form :model="form" :rules="rules" ref="fr" label-position="top" @keyup.enter="submit">
          <el-form-item label="登录账号" prop="username">
            <el-input v-model="form.username" size="large" placeholder="手机号 / 工号" />
          </el-form-item>
          <el-form-item label="登录密码" prop="password">
            <el-input v-model="form.password" type="password" size="large" show-password placeholder="请输入密码" />
          </el-form-item>
          <div class="role-label">登录身份（点击快速填入）</div>
          <div class="roles">
            <span v-for="r in roleList" :key="r.text" :class="{ on: form.username === r.user }" @click="pick(r)">{{ r.text }}</span>
          </div>
          <el-button type="primary" size="large" class="go" :loading="loading" @click="submit">登录系统</el-button>
          <div class="hint">默认账号 admin / 123456　·　会员账号 13800002210 / 123456</div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()
const loading = ref(false)
const fr = ref()
const form = reactive({ username: 'admin', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const roleList = [
  { text: '管理员', user: 'admin' },
  { text: '店长', user: 'manager01' },
  { text: '运营', user: 'operator' },
  { text: '营销', user: 'marketer01' },
  { text: '导购', user: 'guide01' },
  { text: '会员家长', user: 'user' }
]
function pick(r) {
  form.username = r.user
  form.password = '123456'
}

async function submit() {
  const ok = await fr.value.validate().catch(() => false)
  if (!ok) return
  loading.value = true
  try {
    const data = await store.login(form)
    ElMessage.success(`欢迎回来，${data.user.realName || data.user.username}`)
    const isMember = (data.roles || []).includes('MEMBER')
    router.push(route.query.redirect || (isMember ? '/portal/mall' : '/dashboard'))
  } catch (e) {
    /* 错误提示已由拦截器统一处理 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  min-height: 100vh;
  background: #fff;
}
.left {
  position: relative;
  padding: 48px 56px;
  color: #fff;
  display: flex;
  flex-direction: column;
  background:
    linear-gradient(150deg, rgba(200, 107, 133, 0.94), rgba(217, 139, 160, 0.9) 45%, rgba(239, 197, 210, 0.86)),
    url('/login-bg.jpg') center/cover no-repeat;
  overflow: hidden;
}
.left::after {
  content: '';
  position: absolute;
  width: 460px; height: 460px; border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.28);
  right: -160px; top: -110px;
}
.brand { display: flex; align-items: center; gap: 12px; font-size: 18px; font-weight: 700; letter-spacing: 2px; }
.brand .logo {
  width: 34px; height: 34px; border-radius: 12px; background: rgba(255, 255, 255, 0.22);
  display: flex; align-items: center; justify-content: center; font-size: 17px;
}
.brand small { display: block; font-size: 9px; letter-spacing: 1.6px; opacity: 0.8; font-weight: 400; }
.copy { margin-top: auto; }
.copy h1 { font-size: 38px; line-height: 1.34; letter-spacing: 1px; margin: 0 0 16px; font-weight: 700; }
.copy p { font-size: 14px; line-height: 1.8; opacity: 0.94; max-width: 430px; margin: 0; }
.kpis { display: flex; gap: 34px; margin-top: 36px; }
.kpis b { display: block; font-size: 26px; letter-spacing: -0.5px; }
.kpis i { font-size: 12px; opacity: 0.86; font-style: normal; }
.tip { margin-top: auto; font-size: 11.5px; opacity: 0.8; padding-top: 32px; }
.right { display: flex; align-items: center; justify-content: center; padding: 40px; }
.form { width: 360px; }
.form h2 { font-size: 25px; margin: 0 0 6px; letter-spacing: 0.5px; }
.form .sub { font-size: 12.5px; color: var(--muted); margin: 0 0 26px; }
.role-label { font-size: 12.5px; color: var(--muted); margin: 4px 0 8px; }
.roles { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 18px; }
.roles span {
  height: 34px; border: 1px solid var(--line); border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  font-size: 12.5px; color: var(--muted); cursor: pointer; transition: 0.15s;
}
.roles span:hover { border-color: var(--primary-2); color: var(--primary); }
.roles span.on { border-color: var(--primary); color: var(--primary); background: #fbf3f5; font-weight: 600; }
.go { width: 100%; height: 46px; font-size: 15px; margin-top: 6px; border-radius: 10px; }
.hint { font-size: 11.5px; color: #9aa4ac; margin-top: 16px; text-align: center; }

@media (max-width: 900px) {
  .login { grid-template-columns: 1fr; }
  .left { padding: 28px 24px; min-height: 220px; }
  .copy h1 { font-size: 26px; }
  .copy p, .kpis, .tip { display: none; }
  .right { padding: 28px 22px 48px; }
  .form { width: 100%; max-width: 420px; }
}
</style>
