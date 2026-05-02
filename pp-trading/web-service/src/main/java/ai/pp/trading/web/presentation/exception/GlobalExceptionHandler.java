package ai.pp.trading.web.presentation.exception;

import ai.pp.trading.common.dto.ErrorResponse;
import ai.pp.trading.web.infrastructure.client.MarketDataClient.MarketDataClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * 全局异常处理器。
 * 统一捕获异常并返回标准的 ErrorResponse 格式，避免将内部堆栈暴露给前端。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 下游 market-data-service 调用失败 → 503 Service Unavailable
     */
    @ExceptionHandler(MarketDataClientException.class)
    public ResponseEntity<ErrorResponse> handleMarketDataClientException(MarketDataClientException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                e.getMessage(),
                LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * 缺少必填请求参数 → 400 Bad Request
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Missing required parameter: " + e.getParameterName(),
                LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * 兜底：未预料的异常 → 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
