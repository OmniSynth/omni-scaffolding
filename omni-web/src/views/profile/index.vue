<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { changePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const pwdFormRef = ref<FormInstance>()

const genderLabel: Record<string, string> = {
  UNKNOWN: '未知',
  MALE: '男',
  FEMALE: '女',
}

const profile = computed(() => userStore.profile)

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '新密码至少 6 位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function refresh() {
  loading.value = true
  try {
    await userStore.loadMe(true)
  } finally {
    loading.value = false
  }
}

async function submitPassword() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate()
  await changePassword({
    oldPassword: pwdForm.oldPassword,
    newPassword: pwdForm.newPassword,
  })
  ElMessage.success('密码已修改，请牢记新密码')
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value.resetFields()
}

onMounted(refresh)
</script>

<template>
  <div class="profile-page" v-loading="loading">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="card-header">个人资料</div>
      </template>
      <div class="profile-head">
        <el-avatar :size="72" :src="profile?.avatarUrl || undefined">
          {{ (userStore.displayName || '?').slice(0, 1) }}
        </el-avatar>
        <div class="profile-head-text">
          <div class="name">{{ userStore.displayName }}</div>
          <div class="sub">@{{ profile?.username }}</div>
        </div>
      </div>
      <el-descriptions :column="2" border class="desc">
        <el-descriptions-item label="用户名">{{ profile?.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ profile?.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile?.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ genderLabel[profile?.gender || 'UNKNOWN'] || profile?.gender || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">{{ profile?.mobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ profile?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ profile?.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位">
          {{ (profile?.posts || []).length ? profile?.posts?.join('、') : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          {{ (profile?.roles || []).length ? profile?.roles?.join('、') : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="数据范围">{{ profile?.dataScope || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="card">
      <template #header>
        <div class="card-header">修改密码</div>
      </template>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 480px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitPassword">保存密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card-header {
  font-weight: 600;
  color: #0f172a;
}
.profile-head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.profile-head-text .name {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}
.profile-head-text .sub {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}
.desc {
  margin-top: 4px;
}
</style>
