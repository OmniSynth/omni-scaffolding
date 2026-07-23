<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  changeUserEnabled,
  createUser,
  exportUsers,
  getUser,
  listUsers,
  removeUser,
  resetUserPassword,
  updateUser,
} from '@/api/system/user'
import { listRoles } from '@/api/system/role'
import { listPosts } from '@/api/system/post'
import { fetchDeptTree } from '@/api/system/dept'
import { useUserStore } from '@/stores/user'
import type { DeptView, PostView, RoleView, UserDetailView } from '@/types/api'
import FileUpload from '@/components/FileUpload.vue'
import FileImage from '@/components/FileImage.vue'
import DictTag from '@/components/DictTag.vue'
import { useDict } from '@/composables/useDict'

const userStore = useUserStore()
const canEditUser = computed(() => userStore.hasPermission('system:user:edit'))
const { options: genderOptions } = useDict('sys_gender')

const loading = ref(false)
const rows = ref<UserDetailView[]>([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const roles = ref<RoleView[]>([])
const posts = ref<PostView[]>([])
const depts = ref<DeptView[]>([])
const dialogVisible = ref(false)
const pwdVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const statusLoadingId = ref<number | null>(null)
const exporting = ref(false)

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  realName: '',
  mobile: '',
  email: '',
  gender: 'UNKNOWN',
  avatarFileId: null as number | null,
  avatarUrl: '' as string,
  deptId: undefined as number | undefined,
  postIds: [] as number[],
  roleIds: [] as number[],
  enabled: true,
})

const pwdForm = reactive({
  userId: 0,
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  roleIds: [{ required: true, type: 'array', min: 1, message: '请选择角色', trigger: 'change' }],
  mobile: [{ pattern: /^$|^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
}

const pwdRules: FormRules = {
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const data = await listUsers({
      keyword: keyword.value || undefined,
      page: page.value,
      size: size.value,
    })
    rows.value = data.records
    total.value = data.total
    page.value = data.page
    size.value = data.size
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  load()
}

async function onExport() {
  exporting.value = true
  try {
    await exportUsers({ keyword: keyword.value || undefined })
    ElMessage.success('导出成功')
  } catch {
    // request 工具已提示错误
  } finally {
    exporting.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  load()
}

function onSizeChange(s: number) {
  size.value = s
  page.value = 1
  load()
}

async function loadMeta() {
  const [rolePage, postPage, deptTree] = await Promise.all([
    listRoles({ page: 1, size: 200 }),
    listPosts({ page: 1, size: 200 }),
    fetchDeptTree(),
  ])
  roles.value = rolePage.records
  posts.value = postPage.records
  depts.value = deptTree
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    username: '',
    password: '',
    nickname: '',
    realName: '',
    mobile: '',
    email: '',
    gender: 'UNKNOWN',
    avatarFileId: null,
    avatarUrl: '',
    deptId: undefined,
    postIds: [],
    roleIds: [],
    enabled: true,
  })
  dialogVisible.value = true
}

async function openEdit(row: UserDetailView) {
  // 列表字段已脱敏，编辑回填走详情明文接口
  const detail = await getUser(row.id)
  editingId.value = detail.id
  Object.assign(form, {
    username: detail.username,
    password: '',
    nickname: detail.nickname,
    realName: detail.realName || '',
    mobile: detail.mobile || '',
    email: detail.email || '',
    gender: detail.gender || 'UNKNOWN',
    avatarFileId: detail.avatarFileId ?? null,
    avatarUrl: detail.avatarUrl || '',
    deptId: detail.deptId,
    postIds: [...(detail.postIds || [])],
    roleIds: [...(detail.roleIds || [])],
    enabled: detail.enabled,
  })
  dialogVisible.value = true
}

function profilePayload() {
  return {
    nickname: form.nickname,
    realName: form.realName || undefined,
    mobile: form.mobile || undefined,
    email: form.email || undefined,
    gender: form.gender,
    avatarFileId: form.avatarFileId || undefined,
    deptId: form.deptId!,
    postIds: form.postIds,
    roleIds: form.roleIds,
    enabled: form.enabled,
  }
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (editingId.value == null) {
    await createUser({
      username: form.username,
      password: form.password,
      ...profilePayload(),
    })
    ElMessage.success('创建成功')
  } else {
    await updateUser(editingId.value, profilePayload())
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onRemove(row: UserDetailView) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '提示', { type: 'warning' })
  await removeUser(row.id)
  ElMessage.success('已删除')
  await load()
}

function openPwd(row: UserDetailView) {
  pwdForm.userId = row.id
  pwdForm.password = ''
  pwdVisible.value = true
}

async function submitPwd() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate()
  await resetUserPassword(pwdForm.userId, pwdForm.password)
  ElMessage.success('密码已重置')
  pwdVisible.value = false
}

async function onEnabledChange(row: UserDetailView, enabled: boolean | string | number) {
  const next = Boolean(enabled)
  if (row.id === 1 && !next) {
    row.enabled = true
    ElMessage.warning('管理员账号不可停用')
    return
  }
  if (row.id === userStore.profile?.userId && !next) {
    row.enabled = true
    ElMessage.warning('不能停用当前登录用户')
    return
  }
  statusLoadingId.value = row.id
  try {
    await changeUserEnabled(row.id, next)
    row.enabled = next
    ElMessage.success(next ? '已启用' : '已停用')
  } catch {
    row.enabled = !next
  } finally {
    statusLoadingId.value = null
  }
}

onMounted(async () => {
  await Promise.all([load(), loadMeta()])
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="用户名/昵称/姓名/手机/邮箱" style="width: 260px" @keyup.enter="onSearch" />
      <el-button v-permission="'system:user:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button v-permission="'system:user:add'" type="success" @click="openCreate">新增</el-button>
      <el-button v-permission="'system:user:export'" :loading="exporting" @click="onExport">导出</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column label="头像" width="72">
        <template #default="{ row }">
          <FileImage
            :file-id="row.avatarFileId"
            :src="row.avatarUrl"
            :size="36"
            :letter="row.realName || row.nickname || '?'"
          />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="nickname" label="昵称" width="100" />
      <el-table-column prop="mobile" label="手机号" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="160" />
      <el-table-column label="性别" width="80">
        <template #default="{ row }">
          <DictTag type-code="sys_gender" :value="row.gender || 'UNKNOWN'" />
        </template>
      </el-table-column>
      <el-table-column prop="deptName" label="部门" width="120" />
      <el-table-column label="岗位" min-width="120">
        <template #default="{ row }">
          <el-tag v-for="p in row.posts" :key="p" class="tag" size="small" type="warning">{{ p }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="140">
        <template #default="{ row }">
          <el-tag v-for="r in row.roles" :key="r" class="tag" size="small">{{ r }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-switch
            v-if="canEditUser"
            :model-value="row.enabled"
            :disabled="row.id === 1 || row.id === userStore.profile?.userId"
            :loading="statusLoadingId === row.id"
            inline-prompt
            active-text="启"
            inactive-text="停"
            @change="(val: string | number | boolean) => onEnabledChange(row, val)"
          />
          <el-tag v-else :type="row.enabled ? 'success' : 'info'" size="small">
            {{ row.enabled ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:user:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'system:user:resetPwd'" link type="warning" @click="openPwd(row)">重置密码</el-button>
          <el-button v-permission="'system:user:remove'" link type="danger" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增用户' : '编辑用户'" width="640px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="头像">
        <FileUpload
          v-model="form.avatarFileId"
          v-model:preview-url="form.avatarUrl"
          biz-type="avatar"
          tip="jpg/png/gif/webp，最大 2MB"
        />
      </el-form-item>
      <el-form-item v-if="editingId == null" label="用户名" prop="username">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item v-if="editingId == null" label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.realName" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="form.mobile" maxlength="11" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio v-for="opt in genderOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="部门" prop="deptId">
        <el-tree-select
          v-model="form.deptId"
          :data="depts"
          check-strictly
          :props="{ label: 'name', value: 'id', children: 'children' }"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="岗位">
        <el-select v-model="form.postIds" multiple clearable style="width: 100%">
          <el-option v-for="p in posts" :key="p.id" :label="`${p.name} (${p.code})`" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色" prop="roleIds">
        <el-select v-model="form.roleIds" multiple style="width: 100%">
          <el-option
            v-for="r in roles"
            :key="r.id"
            :label="r.status ? `${r.name} (${r.code})` : `${r.name} (${r.code}) · 已停用`"
            :value="r.id"
            :disabled="!r.status && !form.roleIds.includes(r.id)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.enabled" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="pwdVisible" title="重置密码" width="420px">
    <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
      <el-form-item label="新密码" prop="password">
        <el-input v-model="pwdForm.password" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPwd">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.tag {
  margin-right: 4px;
}
</style>
