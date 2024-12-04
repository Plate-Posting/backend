package com.project.plateposting.api.member.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    private String nickname;
    private String email;
    private String password;
}
