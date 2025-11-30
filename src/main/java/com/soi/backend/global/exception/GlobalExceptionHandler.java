package com.soi.backend.global.exception;

import com.soi.backend.global.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.soi.backend.global.logging.RequestIdFilter.REQUEST_ID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDto<?>> handleCustomException(
            CustomException ex,
            HttpServletRequest req
    ) {
        if (isSwaggerRequest(req)) {
            throw ex; // Swagger는 직접 처리하도록 예외 재던짐
        }

        log.error("[{}] CustomException | message={} | status={} | location={}",
                req.getAttribute(REQUEST_ID),
                ex.getMessage(),
                ex.getHttpStatus(),
                ex.getStackTrace()[0]
        );

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ApiResponseDto.fail(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleException(
            Exception ex,
            HttpServletRequest req
    ) throws Exception {
        if (isSwaggerRequest(req)) {
            throw ex;
        }

        log.error("[{}] UnhandledException | message={} | location={}",
                req.getAttribute(REQUEST_ID),
                ex.getMessage(),
                ex.getStackTrace()[0]
        );

        return ResponseEntity
                .internalServerError()
                .body(ApiResponseDto.fail("서버 오류가 발생했습니다."));
    }

    private boolean isSwaggerRequest(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/swagger-ui") ||
                uri.startsWith("/swagger-resources");
    }
}