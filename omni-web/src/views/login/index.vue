<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { fetchCaptcha } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaEnabled = ref(false)
const captchaImage = ref('')
const captchaLoading = ref(false)

const form = reactive({
  username: 'admin',
  // 生产经 OMNI_ADMIN_INITIAL_PASSWORD 初始化后不再是 admin123；勿预填以免误导
  password: import.meta.env.DEV ? 'admin123' : '',
  captchaId: '',
  captchaCode: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [
    {
      validator: (_rule, value, callback) => {
        if (captchaEnabled.value && !value) {
          callback(new Error('请输入验证码'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function loadCaptcha() {
  captchaLoading.value = true
  try {
    const data = await fetchCaptcha()
    captchaEnabled.value = !!data.enabled
    form.captchaId = data.captchaId || ''
    captchaImage.value = data.imageBase64 || ''
    form.captchaCode = ''
  } catch {
    captchaEnabled.value = false
    form.captchaId = ''
    captchaImage.value = ''
  } finally {
    captchaLoading.value = false
  }
}

async function onSubmit() {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate()
  loading.value = true
  try {
    const result = await userStore.login({
      username: form.username,
      password: form.password,
      captchaId: captchaEnabled.value ? form.captchaId : undefined,
      captchaCode: captchaEnabled.value ? form.captchaCode : undefined,
    })
    ElMessage.success('登录成功')
    if (result.mustChangePwd) {
      await router.replace({ path: '/profile', query: { forcePwd: '1' } })
      return
    }
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/home'
    await router.replace(redirect)
  } catch {
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<template>
  <div class="login-page">
    <div class="panel">
      <h1>Omni Admin</h1>
      <p class="hint">本地默认 admin / admin123；生产请用部署时的初始密码</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-form-item v-if="captchaEnabled" label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" maxlength="8" placeholder="请输入验证码" />
            <button
              type="button"
              class="captcha-btn"
              :disabled="captchaLoading"
              title="刷新验证码"
              @click="loadCaptcha"
            >
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <el-icon v-else><Refresh /></el-icon>
            </button>
          </div>
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background:
    radial-gradient(circle at 20% 20%, rgba(14, 165, 233, 0.25), transparent 40%),
    radial-gradient(circle at 80% 0%, rgba(99, 102, 241, 0.2), transparent 35%),
    linear-gradient(160deg, #0f172a, #1e293b 55%, #0f172a);
}
.panel {
  width: 380px;
  padding: 32px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.35);
}
h1 {
  margin: 0 0 8px;
  font-size: 28px;
  color: #0f172a;
}
.hint {
  margin: 0 0 24px;
  color: #64748b;
  font-size: 13px;
}
.submit {
  width: 100%;
  margin-top: 8px;
}
.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.captcha-btn {
  flex: 0 0 120px;
  height: 32px;
  padding: 0;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #f8fafc;
  cursor: pointer;
  overflow: hidden;
  display: grid;
  place-items: center;
}
.captcha-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
</style>
