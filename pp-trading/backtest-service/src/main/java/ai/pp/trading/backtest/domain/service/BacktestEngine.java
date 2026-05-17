package ai.pp.trading.backtest.domain.service;

import ai.pp.trading.backtest.domain.model.BacktestTradeDetail;
import ai.pp.strategy.domain.indicator.IndicatorData;
import ai.pp.strategy.domain.model.KlineData;
import ai.pp.strategy.domain.model.TradeSignal;
import ai.pp.strategy.domain.strategy.TradingStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 回测引擎核心逻辑
 * 逐根K线模拟交易，生成交易信号列表
 */
@Service
public class BacktestEngine {

    /**
     * 执行回测
     *
     * @param klines     K线数据列表
     * @param indicators 指标数据
     * @param strategy   交易策略
     * @return 交易明细列表
     */
    public List<BacktestTradeDetail> run(List<KlineData> klines,
                                         IndicatorData indicators,
                                         TradingStrategy strategy) {
        List<BacktestTradeDetail> trades = new ArrayList<>();
        TradeSignal position = null;
        int openIndex = -1;

        for (int i = 0; i < klines.size(); i++) {
            if (position == null) {
                // 无持仓，检查开仓信号
                Optional<TradeSignal> openSignal = strategy.checkOpenSignal(klines, indicators, i, false);
                if (openSignal.isPresent()) {
                    position = openSignal.get();
                    openIndex = i;
                }
            } else {
                // 有持仓，检查平仓信号
                Optional<TradeSignal> closeSignal = strategy.checkCloseSignal(klines, indicators, i, position);
                if (closeSignal.isPresent()) {
                    // 有平仓信号，创建交易明细
                    BacktestTradeDetail trade = createTradeDetail(
                        klines, position, openIndex, closeSignal.get(), i, true
                    );
                    trades.add(trade);
                    position = null;
                    openIndex = -1;
                }
            }
        }

        // 遍历结束仍有未平仓持仓
        if (position != null) {
            KlineData lastKline = klines.get(klines.size() - 1);
            BacktestTradeDetail trade = new BacktestTradeDetail(
                openIndex,
                -1,
                klines.get(openIndex).date(),
                null,
                position.price(),
                lastKline.close(),
                position.direction().name(),
                position.reason(),
                null,
                false
            );
            trades.add(trade);
        }

        return trades;
    }

    /**
     * 创建已平仓的交易明细
     */
    private BacktestTradeDetail createTradeDetail(List<KlineData> klines,
                                                  TradeSignal openSignal,
                                                  int openIdx,
                                                  TradeSignal closeSignal,
                                                  int closeIdx,
                                                  boolean closed) {
        return new BacktestTradeDetail(
            openIdx,
            closeIdx,
            klines.get(openIdx).date(),
            klines.get(closeIdx).date(),
            openSignal.price(),
            closeSignal.price(),
            openSignal.direction().name(),
            openSignal.reason(),
            closeSignal.reason(),
            closed
        );
    }
}
