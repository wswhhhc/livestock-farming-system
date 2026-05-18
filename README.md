# 智慧养殖管理系统

基于 Spring Boot 3 + Vue 3 的畜牧养殖信息采集与收益预估平台，集成 AI 智能养殖建议。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 (Composition API + `<script setup>`) + Element Plus + ECharts + Vite |
| 后端 | Spring Boot 3.3.5 + Java 21 + MyBatis-Plus + MySQL 8.0 + JWT |
| AI | 硅基流动 API (OpenAI 兼容) — DeepSeek-V3 |
| 部署 | Docker Compose (Nginx + 后端 jar + MySQL) |

## 功能模块

- **家畜种类管理** — 父子分类，支持鸡/鸭/鹅等多层级
- **养殖场地管理** — 场地编号/名称/容量
- **存栏批次管理** — 批次编号、生长阶段、入场/当前数量、预计出栏日
- **存栏变动记录** — 出栏/死亡/转群/补栏，自动更新存栏量
- **成本记录** — 饲料/苗种/防疫/人工成本统计
- **价格规则** — 按品类+月份的浮动定价
- **养殖建议** — AI 自动分析养殖数据生成专业建议 + 系统规则预警
- **收益预估** — 基于存栏、价格、成本数据计算

## 快速开始

### 本地开发

```bash
# 启动后端 (端口 8088)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 启动前端 (端口 5173)
cd frontend
npm run dev
```

或使用 `start.bat` 一键启动。

### Docker 部署

```bash
docker compose up -d
```

访问 `http://localhost`，API 反向代理到后端 8088 端口。

## 项目结构

```
├── frontend/          # Vue 3 前端
├── backend/           # Spring Boot 后端
├── db/                # 数据库初始化脚本
├── docker-compose.yml
└── start.bat / stop.bat
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_URL` | 数据库 JDBC URL | `jdbc:mysql://localhost:3306/livestock_farming` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `252629` |
| `AI_API_KEY` | 硅基流动 API Key | `sk-bncbjax...` |
