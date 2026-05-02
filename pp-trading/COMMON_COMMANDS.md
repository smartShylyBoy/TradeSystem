# 项目常用命令

## 一键启动全部服务
```bash
bash scripts/start-all.sh
```

或使用统一管理脚本：
```bash
bash scripts/manage-services.sh start
```

## 一键关闭全部服务
```bash
bash scripts/stop-all.sh
```

或使用统一管理脚本：
```bash
bash scripts/manage-services.sh stop
```

## 重启全部服务
```bash
bash scripts/manage-services.sh restart
```

## 查看服务状态
```bash
bash scripts/manage-services.sh status
```

## 本地数据库初始化
```bash
docker exec -i trading-timescaledb psql -U trading -d trading_platform < db/init/01_init_kline.sql
```

## 检查 K 线表是否创建成功
```bash
docker exec -i trading-timescaledb psql -U trading -d trading_platform -c "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename IN ('kline_daily','kline_weekly','kline_monthly') ORDER BY tablename;"
```

## 检查 K 线表是否已注册为 hypertable
```bash
docker exec -i trading-timescaledb psql -U trading -d trading_platform -c "SELECT hypertable_name FROM timescaledb_information.hypertables WHERE hypertable_schema = 'public' AND hypertable_name IN ('kline_daily','kline_weekly','kline_monthly') ORDER BY hypertable_name;"
```

## 脚本说明
- `scripts/start-all.sh`：一键启动数据库和项目服务。
- `scripts/stop-all.sh`：一键关闭数据库和项目服务。
- `scripts/manage-services.sh`：统一管理脚本，支持 `start`、`stop`、`restart`、`status`。
- 脚本会优先读取项目根目录下的 `.env` 配置。
- Java 服务会尝试启动 `web-service` 和 `market-data-service`。
- 前端服务会尝试启动 `web-app`。
- 当前某些目录如果还没有完整的可运行配置，脚本会自动跳过并输出提示。
- 服务日志会写入 `scripts/.runtime/logs/`，PID 文件会写入 `scripts/.runtime/pids/`。
