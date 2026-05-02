package ai.pp.trading.marketdata.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * FMP历史价格API响应DTO
 * 映射FMP API返回的历史价格数据结构
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FmpHistoricalResponse {

    /** 股票代码 */
    private String symbol;
    /** 历史价格数据列表 */
    private List<FmpDailyPrice> historical;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public List<FmpDailyPrice> getHistorical() { return historical; }
    public void setHistorical(List<FmpDailyPrice> historical) { this.historical = historical; }

    /**
     * FMP每日价格数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FmpDailyPrice {

        /** 日期 */
        private String date;
        /** 开盘价 */
        private Double open;
        /** 最高价 */
        private Double high;
        /** 最低价 */
        private Double low;
        /** 收盘价 */
        private Double close;
        /** 成交量 */
        private Long volume;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Double getOpen() { return open; }
        public void setOpen(Double open) { this.open = open; }
        public Double getHigh() { return high; }
        public void setHigh(Double high) { this.high = high; }
        public Double getLow() { return low; }
        public void setLow(Double low) { this.low = low; }
        public Double getClose() { return close; }
        public void setClose(Double close) { this.close = close; }
        public Long getVolume() { return volume; }
        public void setVolume(Long volume) { this.volume = volume; }
    }
}
