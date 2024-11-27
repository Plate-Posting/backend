package com.project.plateposting.common.response;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    // 400 Bad Reqeust

    // 401 Unauthorized

    // 404 Not Found
    USER_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 유저를 조회할 수 없습니다."),

    // 500 Server Error

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
