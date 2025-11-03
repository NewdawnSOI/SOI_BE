package com.soi.backend.global.exception;

import com.soi.backend.global.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public abstract class BaseController {

    protected <T> ResponseEntity<ApiResponseDto<T>> handleExecption(Exception e) {
        if (e instanceof CustomException customException) {
            return ResponseEntity.status(customException.getHttpStatus())
                    .body(ApiResponseDto.fail(customException.getMessage()));
        }
        log.error("Unhandled Exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.fail("서버 오류가 발생했습니다."));
    }
}
