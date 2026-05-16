package ai.pp.trading.common.dto.indicator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * RSI指标结果DTO
 * 相对强弱指数，RSI = 100 - 100 / (1 + RS)，RS = 平均涨幅 / 平均跌幅
 */
public class RsiResult {

    /** 6日RSI */
    private final List<Double> rsi6List;
    /** 12日RSI */
    private final List<Double> rsi12List;
    /** 24日RSI */
    private final List<Double> rsi24List;

    @JsonCreator
    public RsiResult(@JsonProperty("rsi6List") List<Double> rsi6List,
                     @JsonProperty("rsi12List") List<Double> rsi12List,
                     @JsonProperty("rsi24List") List<Double> rsi24List) {
        this.rsi6List = rsi6List;
        this.rsi12List = rsi12List;
        this.rsi24List = rsi24List;
    }

    public List<Double> getRsi6List() { return rsi6List; }
    public List<Double> getRsi12List() { return rsi12List; }
    public List<Double> getRsi24List() { return rsi24List; }
}
