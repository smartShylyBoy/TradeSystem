package ai.pp.strategy.domain.strategy;

import ai.pp.strategy.domain.indicator.IndicatorData;
import ai.pp.strategy.domain.model.KlineData;
import ai.pp.strategy.domain.model.TradeSignal;

import java.util.List;
import java.util.Optional;

/**
 * 交易策略接口
 * 定义策略的基本信息和信号检查方法
 */
public interface TradingStrategy {

    /**
     * 获取策略唯一标识
     */
    String getId();

    /**
     * 获取策略名称
     */
    String getName();

    /**
     * 获取策略描述
     */
    String getDescription();

    /**
     * 检查开仓信号
     *
     * @param klines        K线数据列表
     * @param indicators    指标数据
     * @param currentIndex  当前检查的K线索引
     * @param hasPosition   是否已有持仓
     * @return 开仓信号，无信号返回 empty
     */
    Optional<TradeSignal> checkOpenSignal(List<KlineData> klines,
                                          IndicatorData indicators,
                                          int currentIndex,
                                          boolean hasPosition);

    /**
     * 检查平仓信号
     *
     * @param klines        K线数据列表
     * @param indicators    指标数据
     * @param currentIndex  当前检查的K线索引
     * @param openSignal    对应的开仓信号
     * @return 平仓信号，无信号返回 empty
     */
    Optional<TradeSignal> checkCloseSignal(List<KlineData> klines,
                                           IndicatorData indicators,
                                           int currentIndex,
                                           TradeSignal openSignal);
}
