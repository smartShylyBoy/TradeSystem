package ai.pp.strategy.domain.indicator;

import ai.pp.trading.common.dto.indicator.IndicatorResponse;

/**
 * 聚合所有技术指标数据
 * 从 IndicatorResponse 转换而来，提供安全的访问方法
 */
public class IndicatorData {

    private final MacdData macd;
    private final MaData ma;
    private final RsiData rsi;
    private final BollData boll;

    public IndicatorData(MacdData macd, MaData ma, RsiData rsi, BollData boll) {
        this.macd = macd;
        this.ma = ma;
        this.rsi = rsi;
        this.boll = boll;
    }

    public MacdData macd() { return macd; }
    public MaData ma() { return ma; }
    public RsiData rsi() { return rsi; }
    public BollData boll() { return boll; }

    /**
     * 从 IndicatorResponse 构建 IndicatorData
     */
    public static IndicatorData from(IndicatorResponse response) {
        return new IndicatorData(
            new MacdData(
                response.getMacd().getDifList(),
                response.getMacd().getDeaList(),
                response.getMacd().getMacdList()
            ),
            new MaData(
                response.getMa().getMa5List(),
                response.getMa().getMa10List(),
                response.getMa().getMa20List(),
                response.getMa().getMa30List(),
                response.getMa().getMa60List()
            ),
            new RsiData(
                response.getRsi().getRsi6List(),
                response.getRsi().getRsi12List(),
                response.getRsi().getRsi24List()
            ),
            new BollData(
                response.getBoll().getUpperList(),
                response.getBoll().getMiddleList(),
                response.getBoll().getLowerList()
            )
        );
    }
}
