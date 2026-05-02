package ai.pp.trading.web.application.usecase;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.web.domain.port.MarketDataPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 获取 K 线数据的用例。
 * 当前阶段为透传模式，直接委托给 MarketDataPort 调用下游 market-data-service。
 * 后续可在此层增加聚合、缓存、鉴权等业务逻辑。
 */
@Service
public class GetKlineUseCase {

    private final MarketDataPort marketDataPort;

    public GetKlineUseCase(MarketDataPort marketDataPort) {
        this.marketDataPort = marketDataPort;
    }

    /**
     * 执行 K 线查询
     */
    public List<KlineResponse> execute(String symbol, String market, String period,
                                       LocalDate startDate, LocalDate endDate) {
        return marketDataPort.getKlines(symbol, market, period, startDate, endDate);
    }
}
