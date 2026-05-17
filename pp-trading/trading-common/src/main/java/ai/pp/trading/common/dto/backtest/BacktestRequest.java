package ai.pp.trading.common.dto.backtest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 回测请求DTO
 */
public class BacktestRequest {

    /** 股票代码 */
    private final String symbol;
    /** 市场，默认 us */
    private final String market;
    /** K线周期，默认 daily */
    private final String period;
    /** 开始日期 */
    private final String startDate;
    /** 结束日期 */
    private final String endDate;
    /** 策略ID */
    private final String strategyId;

    @JsonCreator
    public BacktestRequest(@JsonProperty("symbol") String symbol,
                           @JsonProperty("market") String market,
                           @JsonProperty("period") String period,
                           @JsonProperty("startDate") String startDate,
                           @JsonProperty("endDate") String endDate,
                           @JsonProperty("strategyId") String strategyId) {
        this.symbol = symbol;
        this.market = market != null ? market : "us";
        this.period = period != null ? period : "daily";
        this.startDate = startDate;
        this.endDate = endDate;
        this.strategyId = strategyId;
    }

    public String getSymbol() { return symbol; }
    public String getMarket() { return market; }
    public String getPeriod() { return period; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStrategyId() { return strategyId; }
}
