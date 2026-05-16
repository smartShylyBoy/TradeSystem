package ai.pp.trading.common.dto.indicator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * MA均线结果DTO
 * 简单移动平均线，MA(N) = 过去N个收盘价的算术平均
 */
public class MaResult {

    /** 5日均线 */
    private final List<Double> ma5List;
    /** 10日均线 */
    private final List<Double> ma10List;
    /** 20日均线 */
    private final List<Double> ma20List;
    /** 30日均线 */
    private final List<Double> ma30List;
    /** 60日均线 */
    private final List<Double> ma60List;

    @JsonCreator
    public MaResult(@JsonProperty("ma5List") List<Double> ma5List,
                    @JsonProperty("ma10List") List<Double> ma10List,
                    @JsonProperty("ma20List") List<Double> ma20List,
                    @JsonProperty("ma30List") List<Double> ma30List,
                    @JsonProperty("ma60List") List<Double> ma60List) {
        this.ma5List = ma5List;
        this.ma10List = ma10List;
        this.ma20List = ma20List;
        this.ma30List = ma30List;
        this.ma60List = ma60List;
    }

    public List<Double> getMa5List() { return ma5List; }
    public List<Double> getMa10List() { return ma10List; }
    public List<Double> getMa20List() { return ma20List; }
    public List<Double> getMa30List() { return ma30List; }
    public List<Double> getMa60List() { return ma60List; }
}
