package com.project.plateposting.common.response;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    // 400 Bad Request
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "중복된 이메일입니다."),

    // 401 Unauthorized

    // 404 Not Found
    USER_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 유저를 조회할 수 없습니다."),

    // 500 Server Error
    FAIL_REGISTER_USER(HttpStatus.INTERNAL_SERVER_ERROR, "회원 가입을 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
