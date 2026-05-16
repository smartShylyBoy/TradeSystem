package ai.pp.trading.common.dto.indicator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 布林带指标结果DTO
 * 中轨 = MA20，上轨 = 中轨 + 2×标准差，下轨 = 中轨 - 2×标准差
 */
public class BollResult {

    /** 上轨 */
    private final List<Double> upperList;
    /** 中轨（20日均线） */
    private final List<Double> middleList;
    /** 下轨 */
    private final List<Double> lowerList;

    @JsonCreator
    public BollResult(@JsonProperty("upperList") List<Double> upperList,
                      @JsonProperty("middleList") List<Double> middleList,
                      @JsonProperty("lowerList") List<Double> lowerList) {
        this.upperList = upperList;
        this.middleList = middleList;
        this.lowerList = lowerList;
    }

    public List<Double> getUpperList() { return upperList; }
    public List<Double> getMiddleList() { return middleList; }
    public List<Double> getLowerList() { return lowerList; }
}
