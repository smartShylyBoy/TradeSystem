package ai.pp.trading.backtest.presentation.dto;

import ai.pp.trading.backtest.domain.model.BacktestTradeDetail;

import java.util.List;

/**
 * 简化版回测响应DTO
 * 第3期只返回交易信号列表
 */
public class SimpleBacktestResponse {

    /** 股票代码 */
    private final String symbol;
    /** 策略ID */
    private final String strategyId;
    /** 策略名称 */
    private final String strategyName;
    /** 总交易笔数 */
    private final int totalTrades;
    /** 交易明细列表 */
    private final List<BacktestTradeDetail> trades;

    public SimpleBacktestResponse(String symbol, String strategyId, String strategyName,
                                  int totalTrades, List<BacktestTradeDetail> trades) {
        this.symbol = symbol;
        this.strategyId = strategyId;
        this.strategyName = strategyName;
        this.totalTrades = totalTrades;
        this.trades = trades;
    }

    public String getSymbol() { return symbol; }
    public String getStrategyId() { return strategyId; }
    public String getStrategyName() { return strategyName; }
    public int getTotalTrades() { return totalTrades; }
    public List<BacktestTradeDetail> getTrades() { return trades; }
}
