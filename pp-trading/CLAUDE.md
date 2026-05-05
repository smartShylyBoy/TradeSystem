# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

pp-trading is a US stock K-line (candlestick) data platform. The backend fetches historical OHLCV data from the FMP (Financial Modeling Prep) API, caches it in TimescaleDB, and serves it to a React frontend that renders interactive ECharts candlestick charts. The UI is in Chinese; candlestick colors follow the Chinese convention (red = up, green = down).

## Tech Stack

- **Backend:** Java 8, Spring Boot 2.7.18, Maven (multi-module), Lombok, Spring WebFlux (WebClient), Spring Data JDBC
- **Frontend:** React 19, TypeScript 5.9, Vite 7, ECharts 5.6, Bootstrap 5, Axios
- **Database:** TimescaleDB (PostgreSQL 15 extension) via Docker
- **External API:** FMP (financialmodelingprep.com) for market data

## Commands

### Start all services (Docker DB + backend + frontend)
```powershell
.\scripts\start-all.ps1
```

### Stop all services
```powershell
.\scripts\stop-all.ps1
```

### Build backend (all modules)
```bash
mvn clean install
```

### Run individual backend services
```bash
mvn spring-boot:run -pl market-data-service   # port 8182
mvn spring-boot:run -pl web-service           # port 8181
```

### Frontend
```bash
cd web-app
npm install
npm run dev       # dev server (port 3000 per vite.config.ts)
npm run build     # tsc -b && vite build
```

### Database initialization
```bash
docker exec -i trading-timescaledb psql -U trading -d trading_platform < db/init/01_init_kline.sql
```

### Verify hypertables
```bash
docker exec -i trading-timescaledb psql -U trading -d trading_platform -c "SELECT hypertable_name FROM timescaledb_information.hypertables WHERE hypertable_name IN ('kline_daily','kline_weekly','kline_monthly');"
```

## Architecture

### Service Topology

```
React SPA (port 3000)
    -> web-service BFF (port 8181)
        -> market-data-service (port 8182)
            -> TimescaleDB (port 5432)
            -> FMP API (external)
```

### Modules

| Module | Purpose |
|---|---|
| `trading-common` | Shared DTOs: `KlineResponse`, `ErrorResponse` |
| `market-data-service` | Core service — fetches from FMP, caches in TimescaleDB, serves K-line data |
| `web-service` | BFF — thin proxy that forwards frontend requests to market-data-service |
| `strategy-core` | Empty placeholder for future strategy logic |
| `web-app` | React SPA frontend |

### Backend: Hexagonal (Ports & Adapters) Pattern

Both Java services follow this package layout:
- `domain/model/` — Domain entities
- `domain/port/` — Interfaces (outbound ports: `KlineRepository`, `MarketDataProvider`, `MarketDataPort`)
- `application/usecase/` — Business logic (`GetKlineUseCase`)
- `application/exception/` — Business exceptions
- `infrastructure/` — Adapters (persistence via JDBC, external API via WebClient)
- `presentation/controller/` — REST controllers
- `presentation/exception/` — `GlobalExceptionHandler`

**market-data-service** uses a cache-first strategy: check TimescaleDB before calling FMP. Weekly/monthly candles are aggregated in Java from daily data (Monday-based weeks, calendar months). FMP rate limits (HTTP 429) are caught and rethrown as `RateLimitExceededException`.

**web-service** is a pure passthrough BFF. Its `GetKlineUseCase` delegates directly to `MarketDataPort` -> `MarketDataClient` (WebClient calling `http://localhost:8182`).

### API Routes

| Route | Service | Purpose |
|---|---|---|
| `/api/market-data/kline` | market-data-service (8182) | K-line data with cache |
| `/api/web/kline` | web-service (8181) | BFF proxy to above |

Query params: `symbol`, `market` (default "us"), `period` (daily/weekly/monthly), `startDate`, `endDate`

### Frontend Structure

- `api/index.ts` — Axios client, base URL `http://localhost:8181/api/web`
- `pages/KlinePage.tsx` — Main page: search, tab management, data caching
- `components/KlineChart.tsx` — ECharts candlestick + volume sub-chart
- `components/SearchBar.tsx` — Symbol/market/period/date inputs
- `components/StockTabs.tsx` — Multi-stock tab interface

### Database

Three TimescaleDB hypertables (`kline_daily`, `kline_weekly`, `kline_monthly`) with identical schema:
`time TIMESTAMPTZ, symbol VARCHAR(20), market VARCHAR(10), open, high, low, close DOUBLE PRECISION, volume BIGINT`

Unique index on `(symbol, market, time DESC)`. Weekly/monthly tables are populated on-demand via aggregation in Java, not by the database.

## Environment

All config is loaded from `.env` at project root via `spring-dotenv`. Key variables: `FMP_API_KEY`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `SPRING_PROFILES_ACTIVE`.

## Known Issues

- No Maven wrapper (`mvnw`) is committed — `mvn` must be available on PATH. The scripts reference `mvnw.cmd` which doesn't exist.
- No tests exist anywhere in the project.
- Frontend port: `vite.config.ts` sets port 3000, but `start-all.ps1` expects port 5173 (Vite default). The health check in the script may report web-app as not ready.
- Only US market is implemented — `FmpMarketDataProvider` returns empty for non-"us" markets.
