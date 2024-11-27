package com.project.plateposting.common.advice;
import com.project.plateposting.common.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.project.plateposting.common.exception.BaseException;

@RestControllerAdvice
public class ContollerException {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse> handleGlobalException(BaseException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(ApiResponse.fail(exception.getStatusCode(), exception.getMessage()));
    }

}
