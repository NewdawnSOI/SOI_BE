package com.soi.backend.global;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class ApiResponseDto<T> {

    private boolean success;
    private T data;
    private String message;

    // 성공했을떄
    public static<T> ApiResponseDto<T> success(T data,  String message) {
        return new ApiResponseDto<T>(true, data, message);
    }

    // 실패했을때
    public static<T> ApiResponseDto<T> fail(String message) {
        return new ApiResponseDto<T>(false, null, message);
    }
}
