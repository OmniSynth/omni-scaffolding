# 采纳本脚手架（换皮 / 裁剪）

从 clone 到自己的业务工程时，建议按下列清单操作。不必一次做完；按项目需要勾选。

## 1. 改名（包名 / 应用名）

| 项 | 位置 |
|----|------|
| Maven `groupId` / `artifactId` / 版本 | 根 `pom.xml` 与各子模块 `pom.xml` |
| Java 包名 `com.omni.scaffolding` | `omni-*/src/main/java`、测试、`mapper` XML namespace |
| Spring 应用名 / 日志前缀 | `omni-admin/.../application.yml`（`spring.application.name`） |
| 前端标题 | `omni-web/.env*` 的 `VITE_APP_TITLE`；登录页文案 |
| Docker / Compose 服务名 | `docker-compose.yml`、`Dockerfile` |

可用脚本（**请先提交或备份**；脚本只做文本替换提示，复杂重构仍建议 IDE Rename）：

```bash
# Linux / macOS
./scripts/rename-project.sh com.yourcorp.yourapp your-app

# Windows PowerShell
.\scripts\rename-project.ps1 -NewPackage com.yourcorp.yourapp -NewArtifact your-app
```

替换后务必执行：

```bash
mvn -s .mvn/settings.xml -pl omni-admin -am compile -DskipTests
cd omni-web && npm run build
```

## 2. 裁剪可选依赖

| 能力 | 关闭方式 | 可删模块/依赖 |
|------|----------|----------------|
| Demo 业务 | 从 `omni-admin/pom.xml` 去掉 `omni-demo` | 整个 `omni-demo` |
| Kafka | `OMNI_KAFKA_ENABLED=false`（默认） | demo 内 Kafka 演示即可删 |
| Elasticsearch | `OMNI_ELASTICSEARCH_ENABLED=false`（默认） | 同上 |
| Quartz | `OMNI_QUARTZ_ENABLED=false` | 可去掉 `omni-quartz` 依赖与菜单 |
| 代码生成 | 非 prod 才注册；prod 自然不可用 | `modules/tool/gen` |
| 开放 API | 不用第三方调用时可删 `modules/open`、Flyway V4 菜单与 `OpenApiAuthFilter` 相关配置 | 管理端 `views/open/` |

依赖方向仍须：`admin → modules/demo/quartz → framework → common`。

业务 Redis 一律走 `RedisService`（`omni-framework`），不要再注入 `StringRedisTemplate`。

## 3. 生产密钥与登录安全

务必用环境变量覆盖（见 `.env.example`）：

- `OMNI_JWT_SECRET` / `OMNI_SIGN_SECRET`（≥32 字节）
- `OMNI_ADMIN_INITIAL_PASSWORD`
- DB / Redis 密码

建议生产加强（`application-prod.yml` 或环境变量）：

```yaml
omni:
  security:
    captcha:
      enabled: true
    login-lock:
      enabled: true
      max-failures: 5
      lock-seconds: 900
    session-limit:
      enabled: true
      max-devices: 3
    password-policy:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special: false
      force-change-on-create: true
      force-change-on-reset: true
```

开发默认验证码开启、密码策略较宽松（兼容种子账号 `admin123`），按环境收紧即可。

## 4. 数据库

- Schema **只**走 Flyway：`omni-admin/src/main/resources/db/migration/`
- 改表只新增 `V{n}__xxx.sql`，并同步 `src/test/resources/db/migration-h2/`
- 单库开发只注册 `master`，勿配同址 slave

## 5. 前端

- API 基路径由 `omni-web/src/utils/request.ts` 处理
- 菜单 `component` 与 Flyway 一致；权限码与 `v-permission` / `@PreAuthorize` 同名
- 登录加签密钥：`VITE_OMNI_SIGN_SECRET` 与后端 `OMNI_SIGN_SECRET` 一致

## 6. 自检

```text
[ ] 包名/应用名已替换且能编译
[ ] 已去掉不需要的 demo / 中间件
[ ] 生产密钥已外置
[ ] Flyway + H2 同步
[ ] CI 绿（`.github/workflows/ci.yml`）
[ ] 登录：验证码 / 锁定 / 并发设备上限 / 改密策略符合环境预期
```
