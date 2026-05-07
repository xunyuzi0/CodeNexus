# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目定位

CodeNexus (代码枢纽) — 基于 Docker 沙箱的在线编程评测与实时竞技平台，主打"天梯匹配 + 实时竞速"。论文课题：《基于 Docker 沙箱的在线编程评测平台的设计与实现》。

核心理念：极客美学、高并发、强隔离、无状态交互。

## 开发命令

### 前端 (frontend/)

```bash
cd frontend
npm install
npm run dev          # Vite 开发服务器 (端口 3000)
npm run build        # 类型检查 + 生产构建 (并行执行 vue-tsc 和 vite build)
npm run type-check   # 仅 TypeScript 类型检查 (vue-tsc --build)
npm run format       # Prettier 格式化 src/
```

### 后端 (backend/)

```bash
cd backend
mvn compile          # 编译
mvn spring-boot:run  # 启动 (端口 8080)
mvn package          # 打包 JAR
```

### 基础设施

- MySQL 8 → `localhost:13306`，数据库名 `codenexus`
- Redis → `localhost:6380`
- Docker（需拉取 `eclipse-temurin:17-alpine` 镜像，沙箱判题依赖）

## 全栈架构

### 前后端分离与代理

前端 Vite dev server (3000) 代理 API 至后端 (8080)，规则定义在 `frontend/vite.config.ts`：
- `/api/arena/*`、`/api/ws/*` → 保留路径原样转发（竞技场与 WebSocket）
- 其他 `/api/*` → 去掉 `/api` 前缀后转发

部分老接口（UserController）映射为 `/user/*` 而非 `/api/user/*`，新增接口统一使用 `/api/` 前缀。

### 后端架构 (backend/src/main/java/com/xunyu/codenexus/backend/)

分层结构：

- **controller/** → REST 接口入口，统一封装为 `Result<T>` { code, message, data, timestamp }
  - **controller/admin/** → 管理端接口（AdminUserController、AdminProblemController、AdminArenaController、AdminStatsController），所有接口需 `@Protector(role = ADMIN)` 鉴权
- **service/impl/** → 业务逻辑实现（AdminService 处理管理端业务）
- **mapper/** → MyBatis-Plus 数据访问（LambdaQueryWrapper 为主，极少使用 XML）
- **model/entity/** → 数据库实体（14 张表，`@TableLogic` 软删除 + `MetaObjectHandler` 自动填充时间戳）
- **model/dto/** → request 请求体（继承 `BaseQueryRequest` 分页基类）+ response 响应体
  - **model/dto/request/admin/** → 管理端请求体（AdminUserQueryRequest、AdminProblemQueryRequest、AdminArenaQueryRequest、ProblemUpsertRequest）
  - **model/dto/response/admin/** → 管理端响应体（AdminUserVO、AdminProblemVO、AdminArenaRoomVO、AdminStatsOverviewVO 等）
- **model/enums/** → 枚举（UserRoleEnum: USER/ADMIN/BAN）

核心引擎：

- **engine/arena/ArenaMatchEngine** → 纯内存匹配调度，Redis ZSet 跳表检索候选对手，动态分差阈值 `delta = min(50 + waitSeconds/5.0 * 25, 300)`
- **engine/arena/ArenaWebSocketHandler** → 对战 WebSocket 处理器，内存 ConcurrentHashMap 维护房间状态机，2s 高频遥测（LOC/CPM），5s 断线容错期
- **engine/sandbox/DockerSandboxEngine** → Docker 容器生命周期管理（1 CPU / 256MB / 无网络 / 3s 超时 / finally 强制销毁）
- **engine/sandbox/JudgeDispatcher** → 异步判题调度器，线程池从 Redis List 拉取任务

横切关注点：

- **aop/auth/Protector** → 自定义 `@Protector` 注解 + AOP 切面，实时查库校验角色和封禁状态（非仅依赖 JWT claims）
- **aop/logging/Log** → 操作日志注解
- **common/context/UserContext** → ThreadLocal 线程级身份上下文，请求结束自动清理
- **interceptor/JwtInterceptor** → 解析 Authorization Bearer Token，填充 UserContext
- **config/InterceptorConfig** → 拦截器注册，白名单：`/user/login`、`/user/register`
- **exception/GlobalExceptionHandler** → `@RestControllerAdvice` 统一异常处理，业务异常抛 `BusinessException`

定时任务：

- **task/GlobalRankTask** → 每小时全量重算用户全球排名（聚合查询 `SUM(score > myScore OR (score = myScore AND id < myId)) + 1`）

### 前端架构 (frontend/src/)

路由双布局：

- **BasicLayout** → 侧边栏 + 导航栏的仪表盘壳：`/dashboard`、`/problems`、`/favorites`、`/arena`、`/rank`、`/profile`、`/admin/*`（管理员页面）
- **ArenaLayout** → 全屏对战布局：`/battle/matchmaking`、`/battle/lobby/:roomId`、`/battle/room/:roomId`
- 独立路由：`/problems/:id`（沉浸式做题）、`/login`

关键模块：

- **router/guard.ts** → 路由守卫：token 校验 → 缺失 roles 时拉取用户信息 → 未鉴权重定向至 `/login`
- **stores/** → Pinia 状态管理：`user`（鉴权状态/token/角色）、`settings`（编辑器偏好，零侵入 LocalStorage 持久化）、`ui`（侧边栏折叠状态）
- **api/** → 按模块封装的 Axios 请求（8 个模块：admin/arena/auth/dashboard/favorites/problem/rank/user）
- **utils/request.ts** → Axios 深度封装：baseURL `/api`、15s 超时、Bearer Token 注入、401 响应触发 Page Reload 销毁过期状态
- **utils/battle-ws.ts** → WebSocket 客户端封装：事件订阅发布、指数退避重连（最多 5 次）、Token 校验
- **components/code/CodeEditor** → Monaco Editor 集成
- **components/code/JudgePanel** → 判题进度面板
- **components/arena/BattleConsole** → 对战控制台（遥测数据展示）
- **components/admin/** → 管理端组件：AdminDataTable（通用数据表格）、AdminStatCard（统计卡片）、AdminUserDetailDialog（用户详情弹窗）、AdminProblemFormDialog（题目表单弹窗）

## 关键设计决策

### 鉴权链路

```
请求 → JwtInterceptor 解析 Token → ThreadLocal UserContext → @Protector AOP 实时查库校验角色/BAN → 业务逻辑
```

JWT 有效期内无法撤销，因此敏感接口通过 AOP 实时查库探测封禁状态。

### 管理员端权限控制

- 后端：所有 `/api/admin/*` 接口使用 `@Protector(role = UserRoleEnum.ADMIN)` 注解，AOP 切面实时查库校验管理员角色
- 前端：路由 meta 中 `requiresAdmin: true` 标识管理员专属页面，路由守卫根据用户角色控制访问
- 管理端功能模块：用户管理（角色变更/封禁解封）、题库管理（CRUD/发布下线/测试用例）、对战记录（查询/异常检测）、数据统计（概览/趋势/活跃度）

### 判题流程（两阶段异步削峰）

```
提交 → MySQL 落库 PENDING → Redis List 入队 → 立即返回 → 前端轮询 Redis 获取 Checkpoint 进度 → 后端线程池异步拉取并执行
```

### 沙箱隔离

每次执行动态创建 Docker 容器：1 CPU、256MB 内存、禁用 Swap、Network: none（断网防反弹 Shell）、`CompletableFuture` 3000ms 超时熔断、`finally` 强制销毁容器。

### 前端状态熔断

401 响应 → 禁止常规路由跳转 → 强制 Page Reload，彻底销毁浏览器内存中的过期状态实例。

### 防重与并发控制

- 签到防重：Redis `SETNX` 用户标识 + 日期特征构建互斥锁，绑定 TTL 防死锁
- 热力图防重：Redis `Set` 返回值 O(1) 拦截单日同题冗余记录
- 匹配防并发：Redis `SETNX` 分布式锁

## 代码风格

- 前端格式化：Prettier（无分号、单引号、100 字符宽），修改前端代码后运行 `npm run format`
- 后端使用 Lombok 减少样板代码
- ORM：MyBatis-Plus（非 JPA），无 migration 文件，schema 由实体类隐式定义
- 统一响应：所有后端接口返回 `Result<T>`，禁止裸返回
