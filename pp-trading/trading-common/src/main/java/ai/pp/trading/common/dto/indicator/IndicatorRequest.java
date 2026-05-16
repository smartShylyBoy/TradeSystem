package ai.pp.trading.common.dto.indicator;

import ai.pp.trading.common.dto.KlineResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 技术指标计算请求DTO
 * 携带K线数据及股票标识信息，用于计算MACD、MA、RSI、BOLL四类技术指标
 */
public class IndicatorRequest {

    /** K线数据列表 */
    private final List<KlineResponse> klines;
    /** 股票代码（如AAPL），用于数据库缓存标识 */
    private final String symbol;
    /** 市场（如us），用于数据库缓存标识 */
    private final String market;
    /** K线周期（daily/weekly/monthly），用于数据库缓存标识 */
    private final String period;

    @JsonCreator
    public IndicatorRequest(@JsonProperty("klines") List<KlineResponse> klines,
                            @JsonProperty("symbol") String symbol,
                            @JsonProperty("market") String market,
                            @JsonProperty("period") String period) {
        this.klines = klines;
        this.symbol = symbol;
        this.market = market;
        this.period = period;
    }

    public List<KlineResponse> getKlines() { return klines; }
    public String getSymbol() { return symbol; }
    public String getMarket() { return market; }
    public String getPeriod() { return period; }
}
