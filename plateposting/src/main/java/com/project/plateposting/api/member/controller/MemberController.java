package com.project.plateposting.api.member.controller;

import com.project.plateposting.api.member.dto.RegisterRequestDTO;
import com.project.plateposting.api.member.service.MemberService;
import com.project.plateposting.common.response.ApiResponse;
import com.project.plateposting.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member Contoller", description = "Member 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {
    private final MemberService memberService;

    @Operation(
            summary = "Email, Password 회원가입 API",
            description = "JSON으로 Email, Password, Nickname을 받아 회원 가입"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "중복된 데이터가 존재합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 에러로 회원 가입에 실패했습니다."),
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        memberService.registerUser(registerRequestDTO);
        return ApiResponse.success_only(SuccessStatus.CREATE_USER_SUCCESS);
    }
}
