package ai.pp.trading.backtest.infrastructure.converter;

import ai.pp.strategy.domain.indicator.*;
import ai.pp.strategy.domain.model.KlineData;
import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.common.dto.indicator.IndicatorResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO 与领域模型转换器
 */
public final class DataConverter {

    private DataConverter() {
        // 工具类不允许实例化
    }

    /**
     * KlineResponse 列表转 KlineData 列表
     */
    public static List<KlineData> toKlineDataList(List<KlineResponse> responses) {
        return responses.stream()
            .map(DataConverter::toKlineData)
            .collect(Collectors.toList());
    }

    /**
     * 单个 KlineResponse 转 KlineData
     */
    public static KlineData toKlineData(KlineResponse response) {
        return new KlineData(
            response.getDate(),
            response.getOpen(),
            response.getHigh(),
            response.getLow(),
            response.getClose(),
            response.getVolume()
        );
    }

    /**
     * IndicatorResponse 转 IndicatorData
     */
    public static IndicatorData toIndicatorData(IndicatorResponse response) {
        return new IndicatorData(
            toMacdData(response.getMacd()),
            toMaData(response.getMa()),
            toRsiData(response.getRsi()),
            toBollData(response.getBoll())
        );
    }

    private static MacdData toMacdData(ai.pp.trading.common.dto.indicator.MacdResult result) {
        return new MacdData(
            result.getDifList(),
            result.getDeaList(),
            result.getMacdList()
        );
    }

    private static MaData toMaData(ai.pp.trading.common.dto.indicator.MaResult result) {
        return new MaData(
            result.getMa5List(),
            result.getMa10List(),
            result.getMa20List(),
            result.getMa30List(),
            result.getMa60List()
        );
    }

    private static RsiData toRsiData(ai.pp.trading.common.dto.indicator.RsiResult result) {
        return new RsiData(
            result.getRsi6List(),
            result.getRsi12List(),
            result.getRsi24List()
        );
    }

    private static BollData toBollData(ai.pp.trading.common.dto.indicator.BollResult result) {
        return new BollData(
            result.getUpperList(),
            result.getMiddleList(),
            result.getLowerList()
        );
    }
}
