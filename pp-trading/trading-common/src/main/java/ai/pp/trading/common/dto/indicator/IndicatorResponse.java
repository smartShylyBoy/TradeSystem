package ai.pp.trading.common.dto.indicator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 技术指标计算响应DTO
 * 包含MACD、MA、RSI、BOLL四类指标的计算结果，所有List长度与输入K线一致
 */
public class IndicatorResponse {

    /** MACD指标结果（DIF、DEA、MACD柱） */
    private final MacdResult macd;
    /** MA均线结果（MA5/10/20/30/60） */
    private final MaResult ma;
    /** RSI指标结果（RSI6/12/24） */
    private final RsiResult rsi;
    /** 布林带指标结果（上轨、中轨、下轨） */
    private final BollResult boll;

    @JsonCreator
    public IndicatorResponse(@JsonProperty("macd") MacdResult macd,
                             @JsonProperty("ma") MaResult ma,
                             @JsonProperty("rsi") RsiResult rsi,
                             @JsonProperty("boll") BollResult boll) {
        this.macd = macd;
        this.ma = ma;
        this.rsi = rsi;
        this.boll = boll;
    }

    public MacdResult getMacd() { return macd; }
    public MaResult getMa() { return ma; }
    public RsiResult getRsi() { return rsi; }
    public BollResult getBoll() { return boll; }
}
