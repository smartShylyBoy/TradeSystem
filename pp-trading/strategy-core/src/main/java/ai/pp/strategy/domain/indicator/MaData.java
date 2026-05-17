package ai.pp.strategy.domain.indicator;

import java.util.List;

/**
 * MA均线数据封装
 * 提供安全的索引访问方法
 */
public class MaData {

    private final List<Double> ma5List;
    private final List<Double> ma10List;
    private final List<Double> ma20List;
    private final List<Double> ma30List;
    private final List<Double> ma60List;

    public MaData(List<Double> ma5List, List<Double> ma10List, List<Double> ma20List,
                  List<Double> ma30List, List<Double> ma60List) {
        this.ma5List = ma5List;
        this.ma10List = ma10List;
        this.ma20List = ma20List;
        this.ma30List = ma30List;
        this.ma60List = ma60List;
    }

    public List<Double> ma5List() { return ma5List; }
    public List<Double> ma10List() { return ma10List; }
    public List<Double> ma20List() { return ma20List; }
    public List<Double> ma30List() { return ma30List; }
    public List<Double> ma60List() { return ma60List; }

    public Double getMa5At(int index) {
        if (index < 0 || index >= ma5List.size()) {
            return null;
        }
        return ma5List.get(index);
    }

    public Double getMa10At(int index) {
        if (index < 0 || index >= ma10List.size()) {
            return null;
        }
        return ma10List.get(index);
    }

    public Double getMa20At(int index) {
        if (index < 0 || index >= ma20List.size()) {
            return null;
        }
        return ma20List.get(index);
    }

    public Double getMa30At(int index) {
        if (index < 0 || index >= ma30List.size()) {
            return null;
        }
        return ma30List.get(index);
    }

    public Double getMa60At(int index) {
        if (index < 0 || index >= ma60List.size()) {
            return null;
        }
        return ma60List.get(index);
    }
}
