package ai.pp.strategy.domain.indicator;

import java.util.List;

/**
 * MACD指标数据封装
 * 提供安全的索引访问方法
 */
public class MacdData {

    private final List<Double> difList;
    private final List<Double> deaList;
    private final List<Double> macdList;

    public MacdData(List<Double> difList, List<Double> deaList, List<Double> macdList) {
        this.difList = difList;
        this.deaList = deaList;
        this.macdList = macdList;
    }

    public List<Double> difList() { return difList; }
    public List<Double> deaList() { return deaList; }
    public List<Double> macdList() { return macdList; }

    /**
     * 安全获取DIF值，越界或null返回null
     */
    public Double getDifAt(int index) {
        if (index < 0 || index >= difList.size()) {
            return null;
        }
        return difList.get(index);
    }

    /**
     * 安全获取DEA值，越界或null返回null
     */
    public Double getDeaAt(int index) {
        if (index < 0 || index >= deaList.size()) {
            return null;
        }
        return deaList.get(index);
    }

    /**
     * 安全获取MACD柱值，越界或null返回null
     */
    public Double getMacdAt(int index) {
        if (index < 0 || index >= macdList.size()) {
            return null;
        }
        return macdList.get(index);
    }
}
