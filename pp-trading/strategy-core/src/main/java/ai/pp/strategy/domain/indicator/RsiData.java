package ai.pp.strategy.domain.indicator;

import java.util.List;

/**
 * RSI指标数据封装
 * 提供安全的索引访问方法
 */
public class RsiData {

    private final List<Double> rsi6List;
    private final List<Double> rsi12List;
    private final List<Double> rsi24List;

    public RsiData(List<Double> rsi6List, List<Double> rsi12List, List<Double> rsi24List) {
        this.rsi6List = rsi6List;
        this.rsi12List = rsi12List;
        this.rsi24List = rsi24List;
    }

    public List<Double> rsi6List() { return rsi6List; }
    public List<Double> rsi12List() { return rsi12List; }
    public List<Double> rsi24List() { return rsi24List; }

    public Double getRsi6At(int index) {
        if (index < 0 || index >= rsi6List.size()) {
            return null;
        }
        return rsi6List.get(index);
    }

    public Double getRsi12At(int index) {
        if (index < 0 || index >= rsi12List.size()) {
            return null;
        }
        return rsi12List.get(index);
    }

    public Double getRsi24At(int index) {
        if (index < 0 || index >= rsi24List.size()) {
            return null;
        }
        return rsi24List.get(index);
    }
}
