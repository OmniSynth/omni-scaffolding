# omni-web

Omni Scaffolding 管理端前端：Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router + Axios。

当前范围：登录鉴权、动态菜单、按钮级权限（`v-permission`）、**首页工作台**、系统管理（用户/角色/部门/菜单/岗位/字典/参数/公告/文件/定时任务/IP 白名单）、**开放 API（接口目录 / 客户端）**、运维页、代码生成等。

## 首页工作台

登录后默认进入 `/home`，包含：

| 区块 | 说明 |
|------|------|
| 问候与身份 | 昵称、数据范围、角色；权限码详情请到个人中心查看 |
| 快捷入口 | 按菜单 `*:list` 权限过滤（无权限不显示） |
| 未读公告 | 复用 `/system/notices/unread`（仅需登录） |
| 运行态 | 有 `ops:server:query` 时展示 JVM 堆 / 数据源 / Redis 摘要 |

不造假业务大盘指标；完整运维细节仍在「系统详情」等运维页。

## 环境

- Node.js 20.x（当前锁定 Vite 5，兼容 Node 20.18；若用 Vite 8 需 Node ≥20.19）
- 后端默认 `http://localhost:8080`

## 启动

```bash
cd omni-web
npm install
npm run dev
```

浏览器打开 [http://localhost:5173](http://localhost:5173)。

开发代理：`/api` → `http://localhost:8080`（见 `vite.config.ts`）。

默认账号与后端一致：`admin` / `admin123`。

## 构建

```bash
npm run build
npm run preview
```

## 运维页依赖

| 页面 | 后端开关 | 权限 |
|------|----------|------|
| Kafka 发消息 | `omni.kafka.enabled=true` | 已登录 |
| ES 重建索引 | `omni.elasticsearch.enabled=true` | `demo:product:write` |
| ES 搜索 | 同上 | `demo:product:read` |

未开启对应能力时接口返回 404，页面会提示开启开关。

二次开发规范见仓库根目录 [AGENTS.md](../AGENTS.md)。
