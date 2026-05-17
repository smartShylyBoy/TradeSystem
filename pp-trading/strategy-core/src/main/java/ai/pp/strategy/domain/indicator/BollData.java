package ai.pp.strategy.domain.indicator;

import java.util.List;

/**
 * 布林带指标数据封装
 * 提供安全的索引访问方法
 */
public class BollData {

    private final List<Double> upperList;
    private final List<Double> middleList;
    private final List<Double> lowerList;

    public BollData(List<Double> upperList, List<Double> middleList, List<Double> lowerList) {
        this.upperList = upperList;
        this.middleList = middleList;
        this.lowerList = lowerList;
    }

    public List<Double> upperList() { return upperList; }
    public List<Double> middleList() { return middleList; }
    public List<Double> lowerList() { return lowerList; }

    public Double getUpperAt(int index) {
        if (index < 0 || index >= upperList.size()) {
            return null;
        }
        return upperList.get(index);
    }

    public Double getMiddleAt(int index) {
        if (index < 0 || index >= middleList.size()) {
            return null;
        }
        return middleList.get(index);
    }

    public Double getLowerAt(int index) {
        if (index < 0 || index >= lowerList.size()) {
            return null;
        }
        return lowerList.get(index);
    }
}
