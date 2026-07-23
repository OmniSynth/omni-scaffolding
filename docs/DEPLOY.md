# 生产部署（Nginx + Jenkins）

本文说明「**前端静态资源由 Nginx 托管，后端 fat jar 本机监听，Nginx 反代 `/api`**」的常见部署方式，并给出可直接改写的 Nginx / Jenkins 示例。

Docker Compose 一键部署见 [README.md § Docker](../README.md#docker-一键部署后端--中间件)；换皮与密钥清单见 [ADOPT.md](./ADOPT.md)。

---

## 1. 架构

```text
浏览器
  │  http(s)://your.domain
  ▼
Nginx
  ├─ /           → 静态目录（omni-web `dist`）
  └─ /api/**     → proxy_pass → 127.0.0.1:SERVER_PORT（omni-admin jar）
                       │
                       ├─ MySQL（master / 可选 slave）
                       └─ Redis
```

| 组件 | 说明 |
|------|------|
| 前端 | `omni-web` 执行 `npm run build`，产物放到 Nginx `root` |
| 后端 | `omni-admin` fat jar；Jenkins 示例使用 `--spring.profiles.active=dev` |
| 同源 | 前端与 `/api` 同域名时仍须配置 CORS（浏览器会带 `Origin`） |

---

## 2. 前置条件

| 项 | 建议 |
|----|------|
| JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 20+（仅构建前端的机器需要） |
| MySQL 8 | 主库必填；从库可选（单库时**不要**再配同址 slave，避免双连接池） |
| Redis 7 | 必填 |
| Nginx | 托管静态 + 反代 API |

---

## 3. 前端构建与发布

前端与后端已合并进下方 [§6 Jenkins 一键脚本](#6-jenkins-一键部署脚本前后端)。要点：

- 构建前写入 `.env.production`，`VITE_OMNI_SIGN_SECRET` **必须**与 `OMNI_SIGN_SECRET` 一致
- `npm run build` 产物拷贝到 Nginx `root`（示例：`/var/www/html/omni-scaffolding/`）
- 改签名密钥后必须重新构建前端

---

## 4. 后端环境变量

Jenkins 示例使用 **`dev` profile**（[`application-dev.yml`](../omni-admin/src/main/resources/application-dev.yml)）。`dev` 已内置 DB/Redis 默认值，环境变量可覆盖；正式生产请改用 `prod` 并走强密钥注入。

| 变量 | 必填 | 示例（对齐 `application-dev.yml`） | 说明 |
|------|------|-----------------------------------|------|
| `SERVER_PORT` | 建议 | `8322` | 与 Nginx `proxy_pass` 一致 |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | 否* | `192.168.3.10` / `40785` / `omni-scaffolding` | 不设则用 yml 默认 |
| `DB_USER` / `DB_PASSWORD` | 否* | `root` / `SgQHvsy9M7LWKBCz` | 同上 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | 否* | `192.168.3.10` / `40782` / `123456` | 同上 |
| `REDIS_DB` | 否 | `1` | `dev` 默认 `1` |
| `OMNI_SIGN_SECRET` | 建议 | 见脚本 | 须与前端 `VITE_OMNI_SIGN_SECRET` 一致；至少 8 位，且不能与 `OMNI_ADMIN_INITIAL_PASSWORD` 相同 |
| `OMNI_CORS_ORIGINS` | 建议 | `https://omni-scaffolding.irez.cn` | `dev` 已有该默认；改域名时请覆盖 |

\* `dev` 可不 export，直接吃 yml 默认；脚本里显式写出便于对照与排障。

启动成功后日志应出现类似：

```text
CORS allowedOriginPatterns=[..., https://omni-scaffolding.irez.cn, http://omni-scaffolding.irez.cn]
```

完整变量模板见 [.env.example](../.env.example)。

---

## 5. Nginx 示例

站点文件示例（Debian/Ubuntu 常见路径：`/etc/nginx/sites-enabled/omni-scaffolding`）：

```nginx
server {
    listen 80;
    listen [::]:80;

    server_name omni-scaffolding.irez.cn;

    # 前端 dist 目录
    root /var/www/html/omni-scaffolding;
    index index.html;

    # SPA：刷新子路由回退到 index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 反代后端；端口与 SERVER_PORT / Jenkins 启动端口一致
    location ^~ /api {
        proxy_pass http://127.0.0.1:8322;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        # 登录加签头（默认会转发；若被上游/CDN 剥掉可显式透传）
        proxy_set_header X-Omni-Timestamp $http_x_omni_timestamp;
        proxy_set_header X-Omni-Nonce $http_x_omni_nonce;
        proxy_set_header X-Omni-Sign $http_x_omni_sign;
        # 可选：大文件上传超时
        # proxy_read_timeout 120s;
        # client_max_body_size 32m;
    }
}
```

启用并重载：

```bash
sudo nginx -t && sudo systemctl reload nginx
```

### HTTPS（可选）

用 certbot / 自有证书增加 `listen 443 ssl`，并把 `OMNI_CORS_ORIGINS` 写成 `https://...`。反向代理时务必保留 `X-Forwarded-Proto`，便于后端识别协议。

### CORS 排障

| 现象 | 处理 |
|------|------|
| `403` + `Invalid CORS request` | 设置 `OMNI_CORS_ORIGINS` 与浏览器 Origin 一致后重启 jar |
| 临时绕过（不推荐长期） | 在 `location ^~ /api` 增加 `proxy_set_header Origin "";` |

### 登录 401 排障

先看响应 JSON 的 **`message`**（不要只看 HTTP 状态码）：

| `message` | 原因 | 处理 |
|-----------|------|------|
| `缺少登录签名` | 前端未打进 `VITE_OMNI_SIGN_SECRET`，请求无 `X-Omni-*` 头 | 按 Jenkins 脚本重写 `.env.production` 后 **重新 `npm run build` 并拷贝 dist** |
| `登录签名无效` | 前后端 Sign 密钥不一致 | 保证 `OMNI_SIGN_SECRET` ≡ `VITE_OMNI_SIGN_SECRET`，前后端同一次部署 |
| `登录签名已过期` | 服务器与客户端时差 > 5 分钟 | 校准 NTP / 系统时间 |
| `用户名或密码错误` | 账号或密码不对 | **登录密码 = `OMNI_ADMIN_INITIAL_PASSWORD`**（你这边是 `OmniDemo@2026!`），**不是** `admin123`，也不是 `OMNI_SIGN_SECRET`。登录页若仍预填 `admin123` 请改掉。日志有「已将演示 admin 密码替换…」即库已改密 |

浏览器开发者工具 → Network → `login` 请求头中应有：

- `X-Omni-Timestamp`
- `X-Omni-Nonce`
- `X-Omni-Sign`

三者皆无 → 前端构建缺密钥；有但仍 401 → 密钥不一致或密码错误。


---

## 6. Jenkins 一键部署脚本（前后端 · `prod`）

在 Jenkins Freestyle / Pipeline shell 中于**仓库根目录**执行。使用 `--spring.profiles.active=prod`。
日志只走 Logback（`logback-spring.xml` → `/root/.logs/omni-scaffolding/prod/omni-scaffolding.log`），不要把业务清单打到 `System.out`。

```bash
#!/bin/bash
set -euo pipefail

# ========== 1. 运行参数 ==========
export SERVER_PORT=8322
export DB_HOST=172.16.1.4
export DB_PORT=3306
export DB_NAME=omni-scaffolding
export DB_USER=omni
export DB_PASSWORD=123456
# 单库：不要配同址 slave

export REDIS_HOST=172.16.1.4
export REDIS_PORT=6379
export REDIS_PASSWORD=123456

# 登录密码（≥12）；与 SIGN 必须不同
export OMNI_ADMIN_INITIAL_PASSWORD='OmniDemo@2026!'
# JWT ≥32 字节
export OMNI_JWT_SECRET='vWcunHYLV2yGl9xQsjbpBJk8MRT0N7Sg'
# 加签密钥（≥8）；须与前端 VITE_OMNI_SIGN_SECRET 一致
export OMNI_SIGN_SECRET='aS0mF6xP5mX3zS9hX0kN8gR2nC6iI8rC'
export OMNI_CORS_ORIGINS='https://omni-scaffolding.irez.cn'

# ========== 1.1 密钥预检（失败则不必编译/启动）==========
: "${OMNI_JWT_SECRET:?OMNI_JWT_SECRET 未设置}"
: "${OMNI_SIGN_SECRET:?OMNI_SIGN_SECRET 未设置}"
: "${OMNI_CORS_ORIGINS:?OMNI_CORS_ORIGINS 未设置}"
: "${OMNI_ADMIN_INITIAL_PASSWORD:?OMNI_ADMIN_INITIAL_PASSWORD 未设置}"
if [ "${#OMNI_JWT_SECRET}" -lt 32 ]; then
  echo "错误: OMNI_JWT_SECRET 至少 32 字节，当前=${#OMNI_JWT_SECRET}。生成: openssl rand -base64 32" >&2
  exit 1
fi
if [ "${#OMNI_SIGN_SECRET}" -lt 8 ]; then
  echo "错误: OMNI_SIGN_SECRET 至少 8 位，当前=${#OMNI_SIGN_SECRET}" >&2
  exit 1
fi
if [ "${OMNI_SIGN_SECRET}" = "${OMNI_ADMIN_INITIAL_PASSWORD}" ]; then
  echo "错误: OMNI_SIGN_SECRET 不能与 OMNI_ADMIN_INITIAL_PASSWORD 相同" >&2
  exit 1
fi
if [ "${#OMNI_ADMIN_INITIAL_PASSWORD}" -lt 12 ]; then
  echo "错误: OMNI_ADMIN_INITIAL_PASSWORD 至少 12 位，当前=${#OMNI_ADMIN_INITIAL_PASSWORD}" >&2
  exit 1
fi

WEB_DEPLOY_DIR=/var/www/html/omni-scaffolding
JAR_NAME=omni-admin-1.0.0-SNAPSHOT.jar
LOG_DIR=/root/.logs/omni-scaffolding/prod

# ========== 2. JDK / Maven ==========
export JAVA_HOME=/usr/local/jdk-21.0.7
export MAVEN_HOME=/usr/local/apache-maven-3.9.9
export PATH="${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"

# ========== 3. Node（nvm）+ 前端构建发布 ==========
export NVM_DIR="${HOME}/.nvm"
# shellcheck disable=SC1091
[ -s "${NVM_DIR}/nvm.sh" ] && . "${NVM_DIR}/nvm.sh"
[ -s "${NVM_DIR}/bash_completion" ] && . "${NVM_DIR}/bash_completion"

echo "切换到 Node.js 24..."
nvm install 24
nvm use 24
node -v
npm -v

echo "写入前端生产环境变量（Sign 与后端一致）..."
cd omni-web
cat > .env.production <<EOF
VITE_APP_TITLE=Omni Admin
VITE_API_BASE=/api
VITE_OMNI_SIGN_SECRET=${OMNI_SIGN_SECRET}
EOF

echo "安装依赖并构建前端..."
npm install
npm run build

echo "拷贝 dist → ${WEB_DEPLOY_DIR}"
rm -rf "${WEB_DEPLOY_DIR}"
mkdir -p "${WEB_DEPLOY_DIR}"
cp -r dist/* "${WEB_DEPLOY_DIR}/"
cd ..

# ========== 4. 编译后端 ==========
echo "Maven 打包..."
mvn clean package -Dmaven.test.skip=true

# ========== 5. 重启 jar ==========
cd omni-admin/target

pid="$(ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print $2}' || true)"
if [ -z "${pid}" ]; then
  echo "进程不存在"
else
  echo "停止进程 ${pid}"
  # shellcheck disable=SC2086
  kill -9 ${pid}
fi

mkdir -p "${LOG_DIR}"

JAVA_OPTS=(
  -server
  -Xms2g -Xmx2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:InitiatingHeapOccupancyPercent=45
  -XX:+ParallelRefProcEnabled
  -XX:+AlwaysPreTouch
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath="${LOG_DIR}/heapdump.hprof"
  -Xlog:gc*:file="${LOG_DIR}/gc.log:time,uptime,level,tags:filecount=5,filesize=50M"
  -Djava.security.egd=file:/dev/./urandom
  -Dfile.encoding=UTF-8
  -Duser.timezone=Asia/Shanghai
)

# nohup 只收 JVM/GC 杂讯；业务日志由 logback 写到 ${LOG_DIR}/omni-scaffolding.log
BUILD_ID=dontKillMe nohup java \
  "${JAVA_OPTS[@]}" \
  -jar "${JAR_NAME}" \
  --spring.profiles.active=prod \
  >"${LOG_DIR}/nohup.out" 2>&1 &

# ========== 6. 轮询本机端口，确认服务存活（最多 20 次）==========
HEALTH_URL="http://127.0.0.1:${SERVER_PORT}/actuator/health"
echo "等待服务就绪：${HEALTH_URL}"
ready=0
for i in $(seq 1 20); do
  if curl -fsS --connect-timeout 2 --max-time 3 "${HEALTH_URL}" >/dev/null 2>&1; then
    echo "第 ${i} 次检查：服务存活"
    ready=1
    break
  fi
  echo "第 ${i}/20 次检查：未就绪，2s 后重试..."
  sleep 2
done

if [ "${ready}" -ne 1 ]; then
  echo "启动失败：轮询 20 次后本机 ${SERVER_PORT} 仍无响应"
  echo "请查看：${LOG_DIR}/omni-scaffolding.log 与 ${LOG_DIR}/nohup.out"
  exit 1
fi

new_pid="$(ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print $2}' || true)"
echo "启动成功，进程 ID ${new_pid:-未知}"
echo "应用日志：${LOG_DIR}/omni-scaffolding.log"
```

### 脚本注意点

1. **`--spring.profiles.active=prod`**，密钥必须过 §1.1 预检（JWT≥32、SIGN≥8、ADMIN≥12、ADMIN≠SIGN）。
2. **`OMNI_SIGN_SECRET`** ≡ 前端 `VITE_OMNI_SIGN_SECRET`，改密钥后必须重新 `npm run build`。
3. 登录密码 = **`OMNI_ADMIN_INITIAL_PASSWORD`**，不是 `admin123`，也不是 `OMNI_SIGN_SECRET`。
4. 单库不要配同址 `DB_SLAVE_*`。
5. 业务日志只看 Logback 文件：`${LOG_DIR}/omni-scaffolding.log`；`nohup.out` 仅兜底 JVM 标准输出。
6. 启动后轮询 health，最多 20 次、间隔 2s。

---

## 7. 验收清单

- [ ] `curl -sS http://127.0.0.1:8322/actuator/health` 返回 UP
- [ ] 浏览器打开站点域名，静态资源 200
- [ ] 打开开发者工具：登录请求 URL 为同域 `/api/auth/login`，且带 `X-Omni-Sign` 等头
- [ ] 登录成功；若 403 CORS，核对 `OMNI_CORS_ORIGINS`；若 401，看响应 `message`（见上文排障表）
- [ ] 登录签名失败时，核对前后端 `SIGN_SECRET` 是否同一次构建/部署一致
- [ ] 侧栏菜单与权限按钮正常（动态权限依赖 Redis + DB）

---

## 8. 相关路径

| 路径 | 用途 |
|------|------|
| `omni-admin/.../application-prod.yml` | 正式生产配置 |
| `omni-admin/.../logback-spring.xml` | 日志输出（控制台 + 文件） |
| `omni-framework/.../CorsConfig.java` | CORS 实现 |
| `.env.example` | 环境变量模板 |
| [README.md](../README.md) | 总览与 Docker |
| [ADOPT.md](./ADOPT.md) | 换皮 / 裁剪 |
