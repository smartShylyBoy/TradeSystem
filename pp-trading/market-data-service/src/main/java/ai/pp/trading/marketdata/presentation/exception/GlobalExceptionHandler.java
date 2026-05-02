package ai.pp.trading.marketdata.presentation.exception;

import ai.pp.trading.common.dto.ErrorResponse;
import ai.pp.trading.marketdata.application.exception.InvalidPeriodException;
import ai.pp.trading.marketdata.application.exception.RateLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

/**
 * 全局异常处理器
 * 统一处理各类异常并返回标准错误响应格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 处理API速率限制超出异常，返回429状态码 */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    /** 处理请求参数错误异常，返回400状态码 */
    @ExceptionHandler({InvalidPeriodException.class, MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** 处理未预期异常，返回500状态码 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    /** 构建标准错误响应 */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.value(),
                message,
                Instant.now().toString()
        ));
    }
}
