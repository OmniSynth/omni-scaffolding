# ${cfg.functionName} 代码生成说明

- 表名：`${cfg.tableName}`
- 模块：`${cfg.moduleName}` / 业务：`${cfg.businessName}`
- 包名：`${cfg.packageName}`
- 权限前缀：`${perm}`
- API：`${apiPath}`

## 合并步骤

1. 将 `java/` 下源码复制到 `omni-system/src/main/java/`（保持包路径）。
2. 将 `resources/mapper/` 下 XML 复制到 `omni-system/src/main/resources/mapper/`。
3. 将 `omni-web/` 下文件复制到前端工程对应路径。
4. 按需调整 `sql/menu.sql` 中的菜单 ID 后执行；默认挂到角色 `1`（admin）。
5. 在 `omni-web/src/router/index.ts` 的 Layout children 中增加路由（前端当前为静态路由）：

```ts
{
  path: '${routePath}',
  name: '${functionCamel}',
  component: () => import('@/views/${cfg.moduleName}/${cfg.businessName}/index.vue'),
  meta: { title: '${cfg.functionName}', permission: '${perm}:list' },
},
```

6. 重新编译后端、构建前端；重新登录管理端以加载新菜单权限。

## 说明

- 单表 CRUD，不含关联 / 树表 / 导入导出。
- 查询字段按生成配置输出：`EQ` / `LIKE` / `BETWEEN`（时间范围）。
- 若表含 `deleted` 字段则生成逻辑删除；含 `created_at`/`updated_at`/`version` 则实体继承 `BaseAuditableEntity`。
