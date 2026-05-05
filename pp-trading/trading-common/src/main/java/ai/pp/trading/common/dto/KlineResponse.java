package ai.pp.trading.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * K线响应DTO
 * API返回的K线数据格式，包含OHLCV数据
 */
public class KlineResponse {

    /** 日期 */
    private final String date;
    /** 开盘价 */
    private final double open;
    /** 最高价 */
    private final double high;
    /** 最低价 */
    private final double low;
    /** 收盘价 */
    private final double close;
    /** 成交量 */
    private final long volume;

    @JsonCreator
    public KlineResponse(@JsonProperty("date") String date,
                         @JsonProperty("open") double open,
                         @JsonProperty("high") double high,
                         @JsonProperty("low") double low,
                         @JsonProperty("close") double close,
                         @JsonProperty("volume") long volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public String getDate() { return date; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
    public long getVolume() { return volume; }
}
