# AGENTS.md — AI / 协作者开发手册

> 本文件是给 **AI 编码助手**（Cursor、Claude、Copilot、Codex 等）与人类贡献者的**强制规范**。  
> 新增业务前先读完；不确定时对照现有模块（推荐模板：`Post` / `Config` / `Dict`）。

人类可读总览见 [README.md](./README.md)。本文件侧重「怎么写代码才符合本仓库」。

---

## 0. 一句话心智模型

单体多模块 Spring Boot：**写用 JPA（主库）· 复杂读用 MyBatis（从库）· Schema 只走 Flyway · Redis 统一 `RedisService` · API 统一 `ApiResponse` · 权限码进菜单表 · 前端 Vue3 跟后端权限同名。**

禁止把本仓库当成微服务拆分；禁止引入与现有栈冲突的新持久化/安全框架。

---

## 1. 模块与依赖（不可违反）

```text
omni-common      # 无 Spring 业务；DTO 契约、工具、SPI、异常、审计基类
omni-framework   # Spring 装配、Security、infra 实现；禁止依赖 omni-modules / demo
omni-modules     # 业务：modules.system / modules.ops / modules.tool.gen
omni-demo        # 演示；可从 omni-admin 去掉依赖
omni-quartz      # 可选定时任务
omni-admin       # 启动入口 + application*.yml + Flyway
omni-web         # Vue3 管理端（独立 npm，不进 Maven）
```

依赖方向（禁止反向）：

```text
omni-admin → omni-modules / omni-demo / omni-quartz → omni-framework → omni-common
```

| 放哪里 | 放什么 |
|--------|--------|
| `omni-common` | `ApiResponse`、`ErrorCode`、`BusinessException`、`BaseAuditableEntity`、`IdGenerator`、缓存/`RedisKeys`、文件 SPI、通知 SPI、纯工具 |
| `omni-framework` | Security/JWT、**`RedisService`**、限流、锁、Druid/读写切面、Local/MinIO/OSS 实现、全局异常、Jackson 扩展、开放 API 过滤器 |
| `omni-modules` | 具体业务 Controller/Service/Entity/Mapper/DTO（含 `modules.open`） |
| `omni-admin` | 仅启动类、配置、`db/migration`、测试 profile |

包名：`com.omni.scaffolding...`。业务包在 `com.omni.scaffolding.modules.<域>.*`。

---

## 2. 新增一个业务功能的标准清单

以「岗位 Post」为蓝本，新增类似 CRUD 时按顺序做：

### A. 数据库（必须）

1. 新脚本：`omni-admin/src/main/resources/db/migration/V{n}__xxx.sql`（**禁止改已发布的 V1/V2…**）
2. 同步 H2 测试脚本：`omni-admin/src/test/resources/db/migration-h2/`（同版本语义）
3. 表字段惯例：
   - 主键 `id` BIGINT（业务侧用 `IdGenerator.nextId()`，不用 DB 自增）
   - `created_at` / `updated_at` / `version`（对齐 `BaseAuditableEntity`）
   - 逻辑删除 `deleted` TINYINT：`0` 正常 / `1` 删除
4. 菜单 + 按钮权限：`INSERT INTO sys_menu (...)`，并给 ADMIN 角色授权（对照 V1/V2 写法）
5. 权限码格式：`{域}:{资源}:{动作}`，如 `system:post:query|add|edit|remove|export`

### B. 后端（`omni-modules`）

```text
modules/<域>/
  controller/XxxController.java
  service/XxxService.java
  entity/SysXxx.java              # JPA 写模型，继承 BaseAuditableEntity
  repository/SysXxxRepository.java
  mapper/SysXxxQueryMapper.java   # 仅复杂读 + 少量关联写
  dto/<聚合>/XxxView.java
  dto/<聚合>/XxxSaveRequest.java
  dto/excel/XxxExportRow.java     # 可选
resources/mapper/<域>/SysXxxQueryMapper.xml
```

DTO **按聚合分子包**（已有：`dto.user` / `dto.role` / `dto.post` / `dto.auth` / `open.dto.client`…），不要再往 `dto` 根目录堆文件。

### C. 前端（`omni-web`）

1. `src/api/<域>/xxx.ts` — 用 `@/utils/request` 的 `getData/postData/putData/deleteData`
2. `src/views/<域>/xxx/index.vue` — Element Plus + `<script setup>`
3. `src/types/api.ts` — 补类型（与后端 View 字段对齐，驼峰）
4. 菜单 `component` 路径与 Flyway 一致，如 `system/post/index`
5. 按钮用 `v-permission="'system:post:add'"`（与 `perms` 一致）

### D. 验证

- `mvn -pl omni-admin -am test`（至少相关模块可编译）
- 登录后侧栏能看到菜单；无权限按钮不可见；接口 403 符合预期

---

## 3. 双轨持久化（最高优先级）

### 3.1 分工

| 操作 | 技术 | 数据源 |
|------|------|--------|
| 新增 / 修改 / 逻辑删除 / 简单 `findById` | **JPA** `Repository` | **master** |
| 分页、多表 join、动态条件、聚合、导出查询 | **MyBatis** `*QueryMapper` + XML | **slave**（无从库时回落 master） |
| 关联表维护（如 `sys_user_role`） | MyBatis `insert/delete` 允许 | **必须走主库**（见下） |

### 3.2 强制：`saveAndFlush`

同事务内顺序若为：

1. `repository.save(...)`  
2. 立刻 MyBatis `insert/delete` 关联 **或** MyBatis `find/list` 回读详情  

则第 1 步必须是 **`saveAndFlush`**，否则会出现：

- 外键失败（`sys_user_role` 找不到 `user_id`）
- 详情读空 / 业务「已创建但读取失败」

参考：`UserService`、`RoleService`、`PostService`、`modules/package-info.java`。

### 3.3 读写切面

`ReadWriteDataSourceAspect` 仅把 **读方法名**（`find/list/search/count/get/select/...`）路由到 slave。  
`insert/update/delete/clear` **不得**被切到从库。新增 Mapper 方法请遵守命名约定。

### 3.4 禁止事项

- 禁止用 JPA Entity 直接当复杂列表 DTO 返回（用 `*View`）
- 禁止在 MyBatis 里更新审计字段替代 JPA 写主路径（除非明确是日志类物理删除）
- 禁止 `ddl-auto=update`；Schema **只** Flyway
- 单库开发：**不要**注册与 master 同址的 slave（会双池拖慢启动）；`strict=false` 时缺省回落 master

---

## 4. API / Controller 风格

```java
@Tag(name = "Posts")
@RestController
@RequestMapping("/api/system/posts")
@RequiredArgsConstructor
public class PostController {

    @Operation(summary = "岗位分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:post:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<PostView>> list(...) {
        return ApiResponse.ok(postService.list(...));
    }
}
```

约定：

- 路径：`/api/{域}/{资源}`；写操作用 `POST/PUT/DELETE`
- 返回：一律 `ApiResponse.ok(...)`；业务失败抛 `BusinessException(ErrorCode.xxx, "中文消息")`
- 鉴权：`@PreAuthorize("hasAuthority('...')")` 或 `hasAnyAuthority`
- 限流：对外 API 加 `@RateLimiter(name = "api")`
- 写操作审计：`@OperLog(module = "岗位管理", action = "新增")`
- 入参：`@Valid` + Jakarta Validation；分页用 `page`/`size`（Service 内 `PageQuery.of`）
- 类与 public 方法写简短中文 Javadoc（说明权限码与职责）

---

## 5. Service / 异常 / ID

- `@Service` + `@RequiredArgsConstructor`；写方法 `@Transactional`；只读 `@Transactional(readOnly = true)`
- 业务错误：`throw new BusinessException(ErrorCode.NOT_FOUND, "岗位不存在")`  
  常用：`BAD_REQUEST` / `UNAUTHORIZED` / `FORBIDDEN` / `NOT_FOUND` / `CONFLICT` / `INTERNAL_ERROR`
- 主键：`IdGenerator.nextId()`，创建时 `entity.setId(...)` 再 `saveAndFlush`
- 逻辑删除：改 `deleted=1`，查询条件带 `deleted = 0`
- 缓存：改数据后 `@CacheEvict`；Key 常量放 `omni-common` 的 `CacheNames` / `CacheKeys` / `RedisKeys`
- **Redis**：业务与基础设施注入 `RedisService`（`infra.redis`），禁止直接使用 `StringRedisTemplate`（运维/逃生口用 `redisService.template()`）
- 敏感字段：View 上用 `@Desensitize`；详情编辑接口可用 `@WithoutDesensitize`

---

## 6. Entity / Mapper / XML

**Entity**

- `@Entity` `@Table(name = "sys_xxx")`，继承 `BaseAuditableEntity`
- `@Getter @Setter`（Lombok）；字段注释说明业务含义
- 表名/列名：小写蛇形；Java 驼峰

**Repository**

```java
Optional<SysPost> findByIdAndDeleted(Long id, Integer deleted);
boolean existsByCodeAndDeleted(String code, Integer deleted);
```

**QueryMapper**

- 接口在 `mapper` 包，XML 在 `resources/mapper/<域>/`
- `resultType` 用 DTO 全限定名（分包后注意包名）
- 动态 SQL 风格对齐现有 `SysPostQueryMapper.xml`

---

## 7. 安全与菜单

1. 新接口必须有权限码；菜单 BUTTON 的 `perms` 与 `@PreAuthorize` **字符串完全一致**
2. 给角色 `ADMIN`（通常 `role_id=1`）插入 `sys_role_menu`
3. 动态权限开启时改菜单后依赖缓存失效（已有 `PermissionCacheEvictor`，菜单写路径会调用）
4. 文件访问走统一文件 API（鉴权或短时签名），**不要**再做匿名静态 `/uploads`
5. 可选能力用开关：`omni.kafka.enabled` / `omni.elasticsearch.enabled` / `omni.quartz.enabled`，默认勿强依赖中间件
6. 登录面：验证码 / 失败锁定 / 密码策略见 `omni.security.captcha|login-lock|password-policy`；换皮清单见 [docs/ADOPT.md](./docs/ADOPT.md)

---

## 8. 配置与环境

- 公共：`omni-admin/.../application.yml`
- 环境：`application-dev.yml` / `application-prod.yml` / 测试 `application-test.yml`
- 密钥、DB 密码只用环境变量；**不要**把生产密钥提交进仓库
- MySQL JDBC URL 保留 `openTelemetry=DISABLED`（Connector/J 9 + OTel 会 reverse DNS 拖慢建连）
- 改配置时同步 `.env.example` / README 相关段落（若引入新环境变量）

---

## 9. 前端风格（omni-web）

- Vue 3 + `<script setup lang="ts">` + Element Plus + Pinia
- API 基路径由 `request.ts` 处理（已带 `/api`）；业务 path 写 `/system/posts`
- 列表页结构对齐 `views/system/post/index.vue`：搜索栏、表格、分页、对话框表单
- 权限指令：`v-permission`
- 类型与后端字段驼峰一致；分页字段：`records` / `total` / `page` / `size`
- 不要引入另一套 UI 框架；不要把业务状态塞进无类型的 `any` 深渊

---

## 10. 代码风格（通用）

- 先读再写：优先复制邻近模块模式，而不是发明新分层
- 只改任务相关文件；禁止顺手大重构、禁止无关格式化整文件
- 注释写「为什么 / 约束」，不写废话；对外 API 与非显然逻辑保留 Javadoc
- 命名：清晰英文；用户可见文案用中文
- 日志：业务用 INFO；排障用 DEBUG；禁止吞异常
- 测试：持久化/安全相关逻辑优先补集成或单测；H2 profile 已有样例

---

## 11. 常见坑（AI 易踩）

| 坑 | 正确做法 |
|----|----------|
| `save` 后立刻插关联表 | `saveAndFlush` |
| 所有 `*QueryMapper` 方法都切从库 | 写方法留主库；读方法才切 slave |
| 改旧 Flyway 脚本 | 只新增 `V{n+1}__...sql`，并同步 H2 |
| DTO 平铺在 `dto` 根包 | 按聚合分子包 |
| framework 依赖 modules | 禁止；SPI 放 common，实现放 framework |
| 业务直接 `StringRedisTemplate` | 注入 `RedisService`；复杂运维才用 `template()` |
| 新模块放错 Maven | 业务进 `omni-modules`，不要新建微服务模块除非用户明确要求 |
| 前端 path 与菜单 component 不一致 | 与 Flyway `component` 字段对齐 |
| 为 VT 把连接池开到很大 | 按 DB `max_connections` 与实例数核算 |

---

## 12. 给 AI 的工作流程建议

1. **先定位模板**：`Post*` / `Config*` / `User*`（含关联）三选一对照  
2. **列出文件清单** 再改，避免漏 Flyway / 菜单 / 前端权限  
3. **编译**：`mvn -pl omni-admin -am compile -DskipTests`  
4. **说明**：用中文向用户简述改了什么、如何验证  
5. **提交**：仅在用户明确要求时 `git commit`；不要擅自 push  

开始任务前可用自检：

```text
[ ] Flyway + H2 迁移
[ ] Entity + Repository + QueryMapper/XML
[ ] DTO 分包 + Service（saveAndFlush）+ Controller（权限/限流/OperLog）
[ ] 菜单权限 + ADMIN 授权
[ ] 前端 api + view + types + v-permission
[ ] 未引入反向依赖 / 未改已发布迁移
```

---

## 13. 推荐对照路径（速查）

| 场景 | 对照 |
|------|------|
| 简单 CRUD | `modules/system/**/Post*` + `omni-web/.../post/` |
| 树形 | `Dept*` / `Menu*` |
| 主表 + 关联表 | `UserService`（角色/岗位）+ `RoleService`（菜单） |
| 开放 API | `modules/open/**` + `OpenApiAuthFilter` + `omni-web/views/open/` |
| Redis 读写 | `RedisService`；限流 `RedisRateLimiter`；锁 `DistributedLockService` |
| 字典/参数缓存 | `DictService` / `ConfigService`；前端 `useDict` + `DictTag` |
| 通知通道 SPI | `common.notify.NotifyChannel` + `infra.notify.NotifyDispatcher`（公告发布接入） |
| 文件上传 | `FileService` + `FileUpload.vue` / `FileImage.vue` |
| 可选中间件 | `omni-demo` + `omni.kafka` / `omni.elasticsearch` |
| 读写切面 | `ReadWriteDataSourceAspect` |
| 统一异常 | `GlobalExceptionHandler` |
