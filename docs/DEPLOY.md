# 生产部署（Nginx + Jenkins）

本文说明「**前端静态资源由 Nginx 托管，后端 fat jar 本机监听，Nginx 反代** `/api`」的常见部署方式，并给出可直接改写的 Nginx / Jenkins 示例。

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


| 组件  | 说明                                                               |
| --- | ---------------------------------------------------------------- |
| 前端  | `omni-web` 执行 `npm run build`，产物放到 Nginx `root`                  |
| 后端  | `omni-admin` fat jar；Jenkins 示例使用 `--spring.profiles.active=dev` |
| 同源  | 前端与 `/api` 同域名时仍须配置 CORS（浏览器会带 `Origin`）                         |


---



## 2. 前置条件


| 项       | 建议                                    |
| ------- | ------------------------------------- |
| JDK     | 21+                                   |
| Maven   | 3.9+                                  |
| Node.js | 20+（仅构建前端的机器需要）                       |
| MySQL 8 | 主库必填；从库可选（单库时**不要**再配同址 slave，避免双连接池） |
| Redis 7 | 必填                                    |
| Nginx   | 托管静态 + 反代 API                         |


---



## 3. 前端构建与发布

前端与后端已合并进下方 [§6 Jenkins 一键脚本](#6-jenkins-一键部署脚本前后端)。要点：

- 构建前写入 `.env.production`，`VITE_OMNI_SIGN_SECRET` **必须**与 `OMNI_SIGN_SECRET` 一致
- `npm run build` 产物拷贝到 Nginx `root`（示例：`/var/www/html/omni-scaffolding/`）
- 改签名密钥后必须重新构建前端

---



## 4. 后端环境变量

Jenkins 示例使用 `dev` **profile**（`[application-dev.yml](../omni-admin/src/main/resources/application-dev.yml)`）。`dev` 已内置 DB/Redis 默认值，环境变量可覆盖；正式生产请改用 `prod` 并走强密钥注入。


| 变量                                             | 必填  | 示例（对齐 `application-dev.yml`）                  | 说明                                                                           |
| ---------------------------------------------- | --- | --------------------------------------------- | ---------------------------------------------------------------------------- |
| `SERVER_PORT`                                  | 建议  | `8322`                                        | 与 Nginx `proxy_pass` 一致                                                      |
| `DB_HOST` / `DB_PORT` / `DB_NAME`              | 否*  | `192.168.3.10` / `40785` / `omni-scaffolding` | 不设则用 yml 默认                                                                  |
| `DB_USER` / `DB_PASSWORD`                      | 否*  | `root` / `SgQHvsy9M7LWKBCz`                   | 同上                                                                           |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | 否*  | `192.168.3.10` / `40782` / `123456`           | 同上                                                                           |
| `REDIS_DB`                                     | 否   | `1`                                           | `dev` 默认 `1`                                                                 |
| `OMNI_SIGN_SECRET`                             | 建议  | 见脚本                                           | 须与前端 `VITE_OMNI_SIGN_SECRET` 一致；至少 8 位，且不能与 `OMNI_ADMIN_INITIAL_PASSWORD` 相同 |
| `OMNI_CORS_ORIGINS`                            | 建议  | `https://omni-scaffolding.irez.cn`            | `dev` 已有该默认；改域名时请覆盖                                                          |


 `dev` 可不 export，直接吃 yml 默认；脚本里显式写出便于对照与排障。

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


| 现象                             | 处理                                                    |
| ------------------------------ | ----------------------------------------------------- |
| `403` + `Invalid CORS request` | 设置 `OMNI_CORS_ORIGINS` 与浏览器 Origin 一致后重启 jar          |
| 临时绕过（不推荐长期）                    | 在 `location ^~ /api` 增加 `proxy_set_header Origin "";` |




### 登录 401 排障

先看响应 JSON 的 `message`（不要只看 HTTP 状态码）：


| `message`  | 原因                                             | 处理                                                                                                                                                         |
| ---------- | ---------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `缺少登录签名`   | 前端未打进 `VITE_OMNI_SIGN_SECRET`，请求无 `X-Omni-*` 头 | 按 Jenkins 脚本重写 `.env.production` 后 **重新** `npm run build` **并拷贝 dist**                                                                                     |
| `登录签名无效`   | 前后端 Sign 密钥不一致                                 | 保证 `OMNI_SIGN_SECRET` ≡ `VITE_OMNI_SIGN_SECRET`，前后端同一次部署                                                                                                   |
| `登录签名已过期`  | 服务器与客户端时差 > 5 分钟                               | 校准 NTP / 系统时间                                                                                                                                              |
| `用户名或密码错误` | 账号或密码不对 | **登录密码 =** `OMNI_ADMIN_INITIAL_PASSWORD`，不是演示口令，也不是 `OMNI_SIGN_SECRET`。日志有「已将演示 admin 密码替换…」即库已改密 |


浏览器开发者工具 → Network → `login` 请求头中应有：

- `X-Omni-Timestamp`
- `X-Omni-Nonce`
- `X-Omni-Sign`

三者皆无 → 前端构建缺密钥；有但仍 401 → 密钥不一致或密码错误。

---



## 6. Jenkins 一键部署脚本（前后端 · `prod`）

仓库根目录执行。口令由 Jenkins 注入或在下方参数区填写，**不要把真实口令提交进 Git**。

配置错误看应用日志里的 `[FAIL]` / `[部署配置错误]`（文件：`LOG_DIR/omni-scaffolding.log`）。

```bash
#!/bin/bash
set -euo pipefail

# ======================== 参数（运维只改这一块）========================
export SERVER_PORT=8322
export DB_HOST=
export DB_PORT=3306
export DB_NAME=
export DB_USER=
export DB_PASSWORD=
export REDIS_HOST=
export REDIS_PORT=6379
export REDIS_PASSWORD=
# OMNI_CORS_ORIGINS 非空
export OMNI_CORS_ORIGINS=
# OMNI_ADMIN_INITIAL_PASSWORD ≥12 位，且 ≠ OMNI_SIGN_SECRET
export OMNI_ADMIN_INITIAL_PASSWORD=
# OMNI_JWT_SECRET ≥32 字节
export OMNI_JWT_SECRET=
# OMNI_SIGN_SECRET ≥8 位，且 ≠ OMNI_ADMIN_INITIAL_PASSWORD；前端 VITE_OMNI_SIGN_SECRET 须相同
export OMNI_SIGN_SECRET=

WEB_DEPLOY_DIR=/var/www/html/omni-scaffolding
JAR_NAME=omni-admin-1.0.0-SNAPSHOT.jar
LOG_DIR=/root/.logs/omni-scaffolding/prod
JAVA_HOME=/usr/local/jdk-21.0.7
MAVEN_HOME=/usr/local/apache-maven-3.9.9
# =====================================================================

export PATH="${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"
export NVM_DIR="${HOME}/.nvm"
# shellcheck disable=SC1091
. "${NVM_DIR}/nvm.sh"

: "${DB_HOST:?未设置 DB_HOST}"
: "${DB_NAME:?未设置 DB_NAME}"
: "${DB_USER:?未设置 DB_USER}"
: "${DB_PASSWORD:?未设置 DB_PASSWORD}"
: "${REDIS_HOST:?未设置 REDIS_HOST}"
: "${REDIS_PASSWORD:?未设置 REDIS_PASSWORD}"
: "${OMNI_CORS_ORIGINS:?未设置 OMNI_CORS_ORIGINS}"
: "${OMNI_ADMIN_INITIAL_PASSWORD:?未设置 OMNI_ADMIN_INITIAL_PASSWORD}"
: "${OMNI_JWT_SECRET:?未设置 OMNI_JWT_SECRET}"
: "${OMNI_SIGN_SECRET:?未设置 OMNI_SIGN_SECRET}"

nvm install 24
nvm use 24

cd omni-web
cat > .env.production <<EOF
VITE_APP_TITLE=Omni Admin
VITE_API_BASE=/api
VITE_OMNI_SIGN_SECRET=${OMNI_SIGN_SECRET}
EOF
npm install
npm run build
rm -rf "${WEB_DEPLOY_DIR}" && mkdir -p "${WEB_DEPLOY_DIR}" && cp -r dist/* "${WEB_DEPLOY_DIR}/"
cd ..

mvn clean package -Dmaven.test.skip=true

cd omni-admin/target
pid="$(ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print $2}' || true)"
[ -n "${pid}" ] && kill -9 ${pid} || true
mkdir -p "${LOG_DIR}"

# 丢弃 nohup.out；业务日志见 ${LOG_DIR}/omni-scaffolding.log
BUILD_ID=dontKillMe nohup java \
  -server -Xms2g -Xmx2g -XX:+UseG1GC \
  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath="${LOG_DIR}/heapdump.hprof" \
  -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai \
  -jar "${JAR_NAME}" --spring.profiles.active=prod \
  >/dev/null 2>&1 &

for i in $(seq 1 20); do
  curl -fsS --connect-timeout 2 --max-time 3 "http://127.0.0.1:${SERVER_PORT}/actuator/health" && break
  sleep 2
  [ "$i" -eq 20 ] && { echo "启动失败，看日志: ${LOG_DIR}/omni-scaffolding.log" >&2; exit 1; }
done
echo "启动成功，日志: ${LOG_DIR}/omni-scaffolding.log"
```

---

## 7. 验收清单

- [ ] `curl -sS http://127.0.0.1:8322/actuator/health` 返回 UP
- [ ] 浏览器打开站点域名，静态资源 200
- [ ] 打开开发者工具：登录请求 URL 为同域 `/api/auth/login`，且带 `X-Omni-Sign` 等头
- [ ] 登录成功；若 403 CORS，核对 `OMNI_CORS_ORIGINS`；若 401，看响应 `message`（见上文排障表）
- [ ] 登录签名失败时，核对前后端 `SIGN_SECRET` 是否同一次构建/部署一致
- [ ] 侧栏菜单与权限按钮正常（动态权限依赖 Redis + DB）
- [ ] （若启用开放 API）管理端可维护客户端；`X-Api-Key` 调用 `/api/open/demo/ping` 成功

---

## 8. 相关路径

| 路径 | 用途 |
|------|------|
| `omni-admin/.../application-prod.yml` | 正式生产配置 |
| `omni-admin/.../logback-spring.xml` | 日志（控制台 + 文件） |
| `omni-framework/.../CorsConfig.java` | CORS 实现 |
| `omni-framework/.../infra/redis/RedisService.java` | Redis 统一访问封装 |
| `.env.example` | 环境变量模板 |
| [README.md](../README.md) | 总览与 Docker |
| [ADOPT.md](./ADOPT.md) | 换皮 / 裁剪 |
