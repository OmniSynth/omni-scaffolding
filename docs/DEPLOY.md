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
| `OMNI_SIGN_SECRET` | 建议 | 见脚本 | 须与前端 `VITE_OMNI_SIGN_SECRET` 一致；不设则用 `application.yml` 默认 |
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
| `用户名或密码错误` | 账号或密码不对 | `prod` 登录密码 = `OMNI_ADMIN_INITIAL_PASSWORD`（不是 SIGN）。日志应有「已将演示 admin 密码替换…」 |

浏览器开发者工具 → Network → `login` 请求头中应有：

- `X-Omni-Timestamp`
- `X-Omni-Nonce`
- `X-Omni-Sign`

三者皆无 → 前端构建缺密钥；有但仍 401 → 密钥不一致或密码错误。


---

## 6. Jenkins 一键部署脚本（前后端 · `dev`）

在 Jenkins Freestyle / Pipeline shell 中于**仓库根目录**执行。使用 `--spring.profiles.active=dev`，DB/Redis 对齐 [`application-dev.yml`](../omni-admin/src/main/resources/application-dev.yml)。

```bash
#!/bin/bash
set -euo pipefail

# ========== 1. 运行参数（对齐 application-dev.yml 默认值，可按需覆盖）==========
export SERVER_PORT=8322
export DB_HOST=192.168.3.10
export DB_PORT=40785
export DB_NAME=omni-scaffolding
export DB_USER=root
export DB_PASSWORD='SgQHvsy9M7LWKBCz'
# dev 默认只注册 master，不要配同址 DB_SLAVE_*

export REDIS_HOST=192.168.3.10
export REDIS_PORT=40782
export REDIS_PASSWORD='123456'
export REDIS_DB=1

# 与 application.yml 默认 Sign 一致；前端构建须同源
export OMNI_SIGN_SECRET='aS0mF6xP5mX3zS9hX0kN8gR2nC6iI8rC'
# 与浏览器地址栏一致（dev 已有默认，显式写出便于排障）
export OMNI_CORS_ORIGINS='https://omni-scaffolding.irez.cn'

WEB_DEPLOY_DIR=/var/www/html/omni-scaffolding
JAR_NAME=omni-admin-1.0.0-SNAPSHOT.jar

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

# ========== 5. 重启 jar（profile=dev）==========
cd omni-admin/target

pid="$(ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print $2}' || true)"
if [ -z "${pid}" ]; then
  echo "进程不存在"
else
  echo "停止进程 ${pid}"
  kill -9 "${pid}"
fi

# Jenkins 默认会杀子进程；BUILD_ID=dontKillMe 避免 nohup 被收割
BUILD_ID=dontKillMe nohup java \
  -Xms2048m -Xmx2048m -Xmn2048m \
  -jar "${JAR_NAME}" \
  --spring.profiles.active=dev \
  >/dev/null 2>&1 &

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
  exit 1
fi

new_pid="$(ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print $2}' || true)"
echo "启动成功，进程 ID ${new_pid:-未知}"
```

### 脚本注意点

1. **`--spring.profiles.active=dev`**（吃 `application-dev.yml` 默认 DB/Redis）。
2. **`OMNI_CORS_ORIGINS`** 与浏览器 Origin 一致，否则登录 403。
3. **`OMNI_SIGN_SECRET`** 写入 `.env.production` 的 `VITE_OMNI_SIGN_SECRET`，前后端必须一致。
4. `dev` **不要**配同址 `DB_SLAVE_*`。
5. 应用日志写入 `/root/.logs/omni-scaffolding/{dev|prod}/omni-scaffolding.log`（可用 `OMNI_LOG_HOME` 覆盖根目录）；Jenkins 的 `nohup` 仍可 `>/dev/null`。
6. 启动后轮询本机 `http://127.0.0.1:${SERVER_PORT}/actuator/health`，最多 20 次、间隔 2s。
7. 正式生产请改 `prod`，并强制注入 `OMNI_JWT_SECRET` / `OMNI_SIGN_SECRET` / `OMNI_ADMIN_INITIAL_PASSWORD`。

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
| `omni-admin/.../application-dev.yml` | Jenkins 示例使用的 `dev` 配置 |
| `omni-admin/.../application-prod.yml` | 正式生产配置 |
| `omni-framework/.../CorsConfig.java` | CORS 实现 |
| `.env.example` | 环境变量模板 |
| [README.md](../README.md) | 总览与 Docker |
| [ADOPT.md](./ADOPT.md) | 换皮 / 裁剪 |
