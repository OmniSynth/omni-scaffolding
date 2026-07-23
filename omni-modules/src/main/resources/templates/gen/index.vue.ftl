<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  create${functionCamel},
  list${functionCamel}s,
  remove${functionCamel},
  update${functionCamel},
  type ${functionCamel}View,
} from '@/api/${cfg.moduleName}/${cfg.businessName}'

const loading = ref(false)
const rows = ref<${functionCamel}View[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

<#list queryColumns as col>
<#if col.queryType == "BETWEEN">
const ${col.javaField}Range = ref<[Date, Date] | null>(null)
<#elseif col.javaType == "Boolean">
const ${col.javaField} = ref<boolean | ''>('')
<#elseif col.javaType == "Integer" || col.javaType == "Long" || col.javaType == "BigDecimal">
const ${col.javaField} = ref<number | undefined>(undefined)
<#else>
const ${col.javaField} = ref('')
</#if>
</#list>

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
<#list formColumns as col>
  ${col.javaField}: <#if col.javaType == "Boolean">true<#elseif col.javaType == "Integer" || col.javaType == "Long" || col.javaType == "BigDecimal">undefined as number | undefined<#else>''</#if>,
</#list>
})

const rules: FormRules = {
<#list formColumns as col>
<#if col.required>
  ${col.javaField}: [{ required: true, message: '请输入${col.columnComment}', trigger: '<#if col.javaType == "Boolean" || col.javaType == "Instant">change<#else>blur</#if>' }],
</#if>
</#list>
}

async function load() {
  loading.value = true
  try {
    const data = await list${functionCamel}s({
<#list queryColumns as col>
<#if col.queryType == "BETWEEN">
      ${col.javaField}From: ${col.javaField}Range.value?.[0]?.toISOString(),
      ${col.javaField}To: ${col.javaField}Range.value?.[1]?.toISOString(),
<#elseif col.javaType == "Boolean">
      ${col.javaField}: ${col.javaField}.value === '' ? undefined : ${col.javaField}.value,
<#elseif col.javaType == "Integer" || col.javaType == "Long" || col.javaType == "BigDecimal">
      ${col.javaField}: ${col.javaField}.value,
<#else>
      ${col.javaField}: ${col.javaField}.value || undefined,
</#if>
</#list>
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

function openCreate() {
  editingId.value = null
  Object.assign(form, {
<#list formColumns as col>
    ${col.javaField}: <#if col.javaType == "Boolean">true<#elseif col.javaType == "Integer" || col.javaType == "Long" || col.javaType == "BigDecimal">undefined<#else>''</#if>,
</#list>
  })
  dialogVisible.value = true
}

function openEdit(row: ${functionCamel}View) {
  editingId.value = row.${cfg.pkField} as number
  Object.assign(form, {
<#list formColumns as col>
    ${col.javaField}: row.${col.javaField}<#if col.javaType == "Boolean"> ?? true<#elseif col.javaType != "Integer" && col.javaType != "Long" && col.javaType != "BigDecimal"> ?? ''</#if>,
</#list>
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (editingId.value == null) {
    await create${functionCamel}({ ...form })
    ElMessage.success('创建成功')
  } else {
    await update${functionCamel}(editingId.value, { ...form })
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function onRemove(row: ${functionCamel}View) {
  await ElMessageBox.confirm('确认删除该记录？', '提示', { type: 'warning' })
  await remove${functionCamel}(row.${cfg.pkField} as number)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
<#list queryColumns as col>
<#if col.queryType == "BETWEEN">
      <el-date-picker
        v-model="${col.javaField}Range"
        type="datetimerange"
        range-separator="至"
        start-placeholder="${col.columnComment}起"
        end-placeholder="${col.columnComment}止"
        style="width: 360px"
      />
<#elseif col.javaType == "Boolean">
      <el-select v-model="${col.javaField}" clearable placeholder="${col.columnComment}" style="width: 120px">
        <el-option label="是" :value="true" />
        <el-option label="否" :value="false" />
      </el-select>
<#else>
      <el-input
        v-model="${col.javaField}"
        clearable
        placeholder="${col.columnComment}"
        style="width: 180px"
        @keyup.enter="onSearch"
      />
</#if>
</#list>
      <el-button v-permission="'${perm}:query'" type="primary" @click="onSearch">查询</el-button>
      <el-button v-permission="'${perm}:add'" type="success" @click="openCreate">新增</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe>
<#list listColumns as col>
<#if col.dictType?? && col.dictType?has_content>
      <el-table-column label="${col.columnComment}" min-width="120" show-overflow-tooltip>
        <template v-slot:default="{ row }">
          {{ row.${col.javaField}Text ?? row.${col.javaField} }}
        </template>
      </el-table-column>
<#else>
      <el-table-column prop="${col.javaField}" label="${col.columnComment}" min-width="120" show-overflow-tooltip />
</#if>
</#list>
      <el-table-column label="操作" width="140" fixed="right">
        <template v-slot:default="{ row }">
          <el-button v-permission="'${perm}:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'${perm}:remove'" link type="danger" @click="onRemove(row)">删除</el-button>
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
        @current-change="load"
        @size-change="
          () => {
            page = 1
            load()
          }
        "
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增' : '编辑'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
<#list formColumns as col>
        <el-form-item label="${col.columnComment}" prop="${col.javaField}">
<#if col.javaType == "Boolean">
          <el-switch v-model="form.${col.javaField}" />
<#elseif col.javaType == "Instant">
          <el-date-picker v-model="form.${col.javaField}" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSSZ" style="width: 100%" />
<#elseif col.javaType == "Integer" || col.javaType == "Long" || col.javaType == "BigDecimal">
          <el-input-number v-model="form.${col.javaField}" style="width: 100%" />
<#else>
          <el-input v-model="form.${col.javaField}" />
</#if>
        </el-form-item>
</#list>
      </el-form>
      <template v-slot:footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
