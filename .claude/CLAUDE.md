# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

家畜养殖信息采集及收益估计系统（智慧养殖管理系统）。Spring Boot 3 + Vue 3 全栈项目，Docker Compose 部署。

## 技术栈

- **前端**: Vue 3 (Composition API + `<script setup>`) + Element Plus + ECharts + Vue Router 4 + Axios + Vite
- **后端**: Spring Boot 3.3.5 + Java 21 + MyBatis-Plus + MySQL 8.0 + JWT (jjwt 0.12.6) + Lombok
- **部署**: Docker Compose (Nginx 静态服务 + 后端 jar + MySQL 8.0)

## 开发命令

### 前端 (frontend/)
```bash
npm run dev        # Vite 开发服务器 :5173
npm run build      # 生产构建
npm run preview    # 预览构建产物
```

### 后端 (backend/)
```bash
mvn spring-boot:run          # 开发运行 :8088
mvn package -DskipTests      # 打包 jar
```

### Docker 部署 (项目根目录)
```bash
docker compose up -d       # 启动全部服务
docker compose down        # 停止
```
前端:80 → Nginx 静态服务，API 反向代理到 backend:8088

### 本地开发启动
```bash
start.bat          # 终止旧进程 → 启动后端 → 启动前端
stop.bat           # 终止 8088 和 5173 端口进程
```

## 项目结构

```
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/             # API 接口层 (axios, http.js 含拦截器)
│   │   ├── components/      # 公共组件 (AnimatedNumber, PageHeader)
│   │   ├── composables/     # 组合式函数 (useStage)
│   │   ├── layout/          # 主布局 MainLayout (侧边栏导航)
│   │   ├── router/          # 路由配置 + 登录守卫
│   │   ├── styles/          # 全局样式 (global.css)
│   │   └── views/           # 页面视图 (按模块分目录)
│   ├── Dockerfile           # 多阶段构建: npm ci → build → nginx
│   ├── nginx.conf           # 反向代理 /api/ → backend
│   └── vite.config.js       # 开发代理 /api/ → localhost:8088
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/livestock/
│   │   ├── common/          # Result 统一响应, JwtUtil, JwtAuthInterceptor
│   │   ├── config/          # WebMvcConfig (CORS+拦截器), MyBatisPlus, 全局异常
│   │   ├── controller/      # REST 控制器
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 数据库实体
│   │   ├── mapper/          # MyBatis-Plus Mapper 接口
│   │   └── service/         # 业务逻辑 (impl/)
│   │       └── impl/        # Service 实现
│   ├── src/main/resources/
│   │   ├── application.yml      # 主配置 (数据源, MyBatis-Plus)
│   │   ├── application-dev.yml  # 开发配置 (SQL 日志)
│   │   └── application-prod.yml
│   └── pom.xml
├── db/
│   └── init.sql             # 建表 + 演示数据 (MySQL 8.0+)
├── docker-compose.yml       # mysql:3307, backend:8088, frontend:80
├── create_ppt.py            # PPT 生成脚本
└── start.bat / stop.bat     # Windows 开发启动/停止脚本
```

## 业务模块

1. **家畜种类** (livestock_category) — 支持父子分类 (如 家禽→鸡/鸭/鹅)，定义生长周期、体重标准、饲料消耗、基准价等参数
2. **养殖场地** (breeding_site) — 场地编号/名称/容量
3. **存栏批次** (batch) — 核心模块，含批次编号、当前阶段(苗种/青年/成年/出栏前)、入场/当前数量、预计出栏日
4. **存栏变动** (batch_change) — 出栏/死亡/转群/补栏等变动记录，自动影响批次当前存栏量
5. **成本记录** (cost_record) — 按批次或种类的饲料/苗种/防疫/人工成本
6. **价格规则** (price_rule) — 按品类+月份的条件浮动规则，用于收益预估
7. **养殖建议** (breeding_advice + breeding_advice_template) — 模板驱动，按生长阶段生成建议
8. **收益预估** — 基于存栏量、基准价、价格规则、成本数据计算
9. **系统配置** (system_config) — 键值对配置 (农场名称、联系电话等)
10. **用户认证** (sys_user) — 养殖户(1)/管理员(2)，JWT Bearer Token

## 后端架构模式

- **分层**: Controller → Service (接口+Impl) → Mapper (MyBatis-Plus)
- **统一响应**: `Result<T>` 包装，code=200 成功，data 返回数据
- **认证**: JWT 拦截器，除 `/api/auth/login` 外均需 Bearer Token
- **全局异常**: `GlobalExceptionHandler` 统一处理
- **逻辑删除**: 所有表包含 `deleted` 字段，MyBatis-Plus 自动过滤

## 前端架构模式

- **API 层**: `src/api/http.js` 封装 Axios，自动注入 Token、统一错误处理 (401 跳登录)
- **路由守卫**: `router.beforeEach` 检查 `localStorage` 中的 token
- **页面布局**: `MainLayout.vue` 含侧边栏 (可折叠)，所有业务页面作为其子路由
- **状态管理**: 未使用 Pinia/Vuex，状态通过 API 请求获取，组件内局部管理

## 数据库

- MySQL 8.0+，端口 3307 (Docker 对外)
- utf8mb4 编码，`db/init.sql` 包含完整建表 + 演示数据
- Docker 初次启动自动执行 init.sql
