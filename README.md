# 点餐系统

当前仓库包含一期后台端和业务服务端的基础工程：

- `admin-web`：Vue 3 + Element Plus 管理后台
- `backend`：Spring Boot 3 + Java 17 后端服务
- `docs`：需求文档与技术方案

## 当前目标

一期优先建设后台管理端与业务服务端，覆盖以下基础能力：

- 门店、桌位、设备、商品、会员管理
- 订单、加菜、结账相关服务端能力
- 三语言国际化：`zh-CN`、`en-US`、`th-TH`
- 会员国际手机号支持：首批覆盖中国 `+86` 与泰国 `+66`

## 启动说明

### 后端

```powershell
cd backend
mvn spring-boot:run
```

当前后端默认使用本地开发库 `H2`，启动时会自动执行 [schema.sql](H:\work\other\vdamov4\backend\src\main\resources\schema.sql) 和 [data.sql](H:\work\other\vdamov4\backend\src\main\resources\data.sql)。

默认登录账号：

- `admin / admin123`
- `store.manager / store123`

### 前端

```powershell
cd admin-web
npm install
npm run dev
```

## 后续开发重点

- 将当前 H2 开发库切换到 MySQL
- 落用户、角色、菜单、门店关联的新增编辑接口
- 落订单、加菜、结账、优惠真实业务表和接口
- 接入前端登录态与菜单权限控制
- 接入 WebSocket 实时推送
