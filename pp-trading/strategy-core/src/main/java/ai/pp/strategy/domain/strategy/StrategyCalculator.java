package ai.pp.strategy.domain.strategy;

import ai.pp.strategy.domain.model.KlineData;

import java.util.List;

/**
 * 策略计算工具类
 * 提供常用的计算方法
 */
public final class StrategyCalculator {

    private StrategyCalculator() {
        // 工具类不允许实例化
    }

    /**
     * 获取指定区间内最高价的最大值
     * 从 endIndex 往前取 period 个K线，找 high 的最大值
     *
     * @param klines    K线数据列表
     * @param endIndex  结束索引（包含）
     * @param period    往前取的周期数
     * @param minPeriod 最小要求的数据量，不足则返回 NaN
     * @return 最高价最大值，数据不足返回 Double.NaN
     */
    public static double getValidMaxCloseHighBetweenLastPeriod(List<KlineData> klines,
                                                               int endIndex,
                                                               int period,
                                                               int minPeriod) {
        // 计算起始索引
        int startIndex = Math.max(0, endIndex - period + 1);

        // 计算实际可用数据量
        int actualCount = endIndex - startIndex + 1;

        // 数据不足最小要求
        if (actualCount < minPeriod) {
            return Double.NaN;
        }

        // 遍历找最大值
        double maxHigh = Double.MIN_VALUE;
        for (int i = startIndex; i <= endIndex; i++) {
            double high = klines.get(i).high();
            if (high > maxHigh) {
                maxHigh = high;
            }
        }

        return maxHigh;
    }
}
