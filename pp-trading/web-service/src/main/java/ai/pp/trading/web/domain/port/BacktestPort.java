package ai.pp.trading.web.domain.port;

import ai.pp.trading.common.dto.backtest.BacktestRequest;

import java.util.List;
import java.util.Map;

/**
 * 回测服务端口
 * 定义回测相关的出站接口
 */
public interface BacktestPort {

    /**
     * 执行回测
     *
     * @param request 回测请求
     * @return 回测结果（原始 JSON 响应）
     */
    Map<String, Object> runBacktest(BacktestRequest request);

    /**
     * 获取所有可用策略
     *
     * @return 策略信息列表
     */
    List<Map<String, String>> getStrategies();
}
