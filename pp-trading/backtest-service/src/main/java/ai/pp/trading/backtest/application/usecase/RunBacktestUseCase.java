package ai.pp.trading.backtest.application.usecase;

import ai.pp.trading.backtest.domain.model.BacktestTradeDetail;
import ai.pp.trading.backtest.domain.port.IndicatorPort;
import ai.pp.trading.backtest.domain.port.MarketDataPort;
import ai.pp.trading.backtest.domain.service.BacktestEngine;
import ai.pp.strategy.domain.indicator.IndicatorData;
import ai.pp.strategy.domain.model.KlineData;
import ai.pp.strategy.domain.strategy.TradingStrategy;
import ai.pp.trading.backtest.infrastructure.converter.DataConverter;
import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.common.dto.backtest.BacktestRequest;
import ai.pp.trading.common.dto.indicator.IndicatorResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 执行回测用例
 * 编排: 拉取K线数据 → 计算指标 → 跑回测引擎
 */
@Service
public class RunBacktestUseCase {

    private final MarketDataPort marketDataPort;
    private final IndicatorPort indicatorPort;
    private final BacktestEngine backtestEngine;
    private final Map<String, TradingStrategy> strategyMap;

    public RunBacktestUseCase(MarketDataPort marketDataPort,
                              IndicatorPort indicatorPort,
                              BacktestEngine backtestEngine,
                              List<TradingStrategy> strategies) {
        this.marketDataPort = marketDataPort;
        this.indicatorPort = indicatorPort;
        this.backtestEngine = backtestEngine;
        // 将策略列表转为 Map，便于按 ID 查找
        this.strategyMap = strategies.stream()
            .collect(Collectors.toMap(TradingStrategy::getId, Function.identity()));
    }

    /**
     * 执行回测
     *
     * @param request 回测请求
     * @return 交易明细列表
     * @throws IllegalArgumentException 如果策略不存在
     */
    public List<BacktestTradeDetail> execute(BacktestRequest request) {
        // 1. 查找策略
        TradingStrategy strategy = Optional.ofNullable(strategyMap.get(request.getStrategyId()))
            .orElseThrow(() -> new IllegalArgumentException(
                "策略不存在: " + request.getStrategyId()
            ));

        // 2. 拉取K线数据
        List<KlineResponse> klineResponses = marketDataPort.getKlines(
            request.getSymbol(),
            request.getMarket(),
            request.getPeriod(),
            request.getStartDate(),
            request.getEndDate()
        );

        if (klineResponses.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 计算技术指标
        IndicatorResponse indicatorResponse = indicatorPort.calculateIndicators(klineResponses);

        // 4. 转换为领域模型
        List<KlineData> klines = DataConverter.toKlineDataList(klineResponses);
        IndicatorData indicators = DataConverter.toIndicatorData(indicatorResponse);

        // 5. 执行回测
        return backtestEngine.run(klines, indicators, strategy);
    }

    /**
     * 获取所有可用策略
     */
    public List<TradingStrategy> getAvailableStrategies() {
        return new ArrayList<>(strategyMap.values());
    }
}
