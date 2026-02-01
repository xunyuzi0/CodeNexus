# CodeNexus 后端项目上下文 (Context Map)

## 1. 项目概况

- **项目名称**: CodeNexus (代码枢纽)
- **项目定位**: 基于 Docker 沙箱技术的在线编程评测与实时协同平台。
- **核心技术**: Spring Boot 3, MyBatis-Plus, MySQL, Redis, JWT。
- **设计规划**: 后续将集成 Docker (沙箱) 与 WebSocket (协同)。
- **当前开发阶段**: 基础设施搭建完毕，用户模块 (User Module) 开发完成，鉴权体系完整。

## 2. 核心架构与规范

- **接口风格**: RESTful API
- **响应格式**: 统一封装为 `Result<T>` { code, message, data, timestamp }。
- **鉴权机制**:
    - JWT Token 认证 (Header: `Authorization: Bearer <token>`)。
    - `UserContext` (ThreadLocal) 存储当前请求用户信息。
    - AOP 注解 `@Protector` 控制接口权限 (USER/ADMIN)。
- **异常处理**: `GlobalExceptionHandler` 统一捕获，业务异常抛出 `BusinessException`。

## 3. 目录结构与文件说明 (Java)

### `src/main/java/com/xunyu/codenexus/backend`

#### `config/` (全局配置)

- **`CorsConfig.java`**: 全局跨域配置，允许所有来源和 Header。
- **`InterceptorConfig.java`**: 注册拦截器 (JwtInterceptor)，配置放行白名单 (如 /login, /register)。
- **`MyBatisPlusConfig.java`**: 配置 MyBatis Plus 自动填充策略 (createTime, updateTime)。

#### `interceptor/` (拦截器)

- **`JwtInterceptor.java`**: 请求入口拦截。解析 Header 中的 Token，验证合法性，将用户信息存入 `UserContext`。

#### `aop/` (切面编程)

- **`auth/Protector.java`**: 自定义权限注解，标记在 Controller 方法上。
- **`auth/ProtectorAspect.java`**: 权限切面逻辑。检查登录状态、是否封号 (BAN)、角色权限 (ADMIN)。
- **`logging/Log.java`**: 操作日志注解。

#### `common/` (通用组件)

- **`context/UserContext.java`**: 基于 `ThreadLocal` 的用户上下文工具，用于在 Service 层获取当前登录用户 ID。
- **`result/Result.java`**: 统一 API 响应包装类。
- **`result/ResultCode.java`**: 响应状态码枚举 (200, 400, 401, 403, 500)。

#### `controller/` (控制层)

- **`UserController.java`**: 用户相关接口 (登录、注册、获取当前用户)。

#### `service/` (业务逻辑层)

- **`UserService.java`**: 用户业务接口。
- **`impl/UserServiceImpl.java`**: 用户业务实现。包含注册校验、密码加密 (MD5+Salt)、登录 Token 生成。

#### `mapper/` (持久层)

- **`UserMapper.java`**: 继承 `BaseMapper<User>`，提供数据库 CRUD 能力。

#### `model/` (数据模型)

- **`entity/User.java`**: 数据库表 `user` 的映射实体。
- **`dto/request/BaseQueryRequest.java`**: 分页查询基类 (current, pageSize, sort)。
- **`dto/request/user/`**:
    - `UserLoginRequest.java`: 登录入参。
    - `UserRegisterRequest.java`: 注册入参。
- **`dto/response/`**:
    - **`UserLoginVO.java`**: 登录成功返回的脱敏用户信息 (含 Token)。
- **`enums/UserRoleEnum.java`**: 用户角色枚举 (USER, ADMIN, BAN)。

#### `utils/` (工具类)

- **`AssertUtil.java`**: 链式断言工具，校验失败直接抛出 BusinessException (400)。
- **`JwtUtil.java`**: JWT 生成与解析工具 (使用 HMAC-SHA 算法)。

#### `exception/` (异常处理)

- **`BusinessException.java`**: 自定义业务异常。
- **`GlobalExceptionHandler.java`**: 全局异常处理器，将异常转换为 JSON Result 返回。

### `src/main/resources`

- **`application.properties`**: 配置文件 (端口 8080, MySQL 3306映射, Redis 6380映射, JWT 密钥)。
- **`mapper/UserMapper.xml`**: UserMapper 的 XML 映射文件。