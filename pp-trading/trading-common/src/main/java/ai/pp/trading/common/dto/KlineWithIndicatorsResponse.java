package ai.pp.trading.common.dto;

import ai.pp.trading.common.dto.indicator.IndicatorResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * K线+技术指标聚合响应DTO
 * 将K线数据与MACD、MA、RSI、BOLL四类技术指标合并返回
 */
public class KlineWithIndicatorsResponse {

    /** K线数据列表 */
    private final List<KlineResponse> klines;
    /** 技术指标（MACD、MA、RSI、BOLL） */
    private final IndicatorResponse indicators;
    /** K线总条数 */
    private final int totalCount;

    @JsonCreator
    public KlineWithIndicatorsResponse(@JsonProperty("klines") List<KlineResponse> klines,
                                       @JsonProperty("indicators") IndicatorResponse indicators,
                                       @JsonProperty("totalCount") int totalCount) {
        this.klines = klines;
        this.indicators = indicators;
        this.totalCount = totalCount;
    }

    public List<KlineResponse> getKlines() { return klines; }
    public IndicatorResponse getIndicators() { return indicators; }
    public int getTotalCount() { return totalCount; }
}
