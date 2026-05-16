package ai.pp.trading.common.dto.indicator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * MACD指标结果DTO
 * DIF = EMA(12) - EMA(26)，DEA = DIF的9日EMA，MACD柱 = (DIF - DEA) × 2
 */
public class MacdResult {

    /** DIF线（快线） */
    private final List<Double> difList;
    /** DEA线（慢线/信号线） */
    private final List<Double> deaList;
    /** MACD柱状图 */
    private final List<Double> macdList;

    @JsonCreator
    public MacdResult(@JsonProperty("difList") List<Double> difList,
                      @JsonProperty("deaList") List<Double> deaList,
                      @JsonProperty("macdList") List<Double> macdList) {
        this.difList = difList;
        this.deaList = deaList;
        this.macdList = macdList;
    }

    public List<Double> getDifList() { return difList; }
    public List<Double> getDeaList() { return deaList; }
    public List<Double> getMacdList() { return macdList; }
}
