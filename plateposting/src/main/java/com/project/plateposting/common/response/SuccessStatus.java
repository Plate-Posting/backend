package com.project.plateposting.common.response;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    // 200 Ok
    CREATE_USER_SUCCESS(HttpStatus.OK, "유저 생성 성공"),

    GET_POST_SUCCESS(HttpStatus.OK, "게시글 조회 성공"),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }

}
