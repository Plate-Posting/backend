package com.project.plateposting.api.member.service;

import com.project.plateposting.api.member.dto.RegisterRequestDTO;
import com.project.plateposting.api.member.entity.Member;
import com.project.plateposting.api.member.entity.Role;
import com.project.plateposting.api.member.repository.MemberRepository;
import com.project.plateposting.common.exception.BadRequestException;
import com.project.plateposting.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public void registerUser(RegisterRequestDTO registerRequestDTO) {
        Member newMember = Member.builder()
                .nickname(registerRequestDTO.getNickname())
                .email(registerRequestDTO.getEmail())
                .password(registerRequestDTO.getPassword())
                .role(Role.GUEST)
                .build();

        checkEmail(newMember.getEmail());
        try {
            memberRepository.save(newMember);
        } catch (BadRequestException e) {
            log.error(ErrorStatus.FAIL_REGISTER_USER.getMessage(), e);
            throw new BadRequestException(ErrorStatus.FAIL_REGISTER_USER.getMessage());
        }
    }

    @Transactional
    public void checkEmail(String email) {
        validEmail(email);
    }

    private void validEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BadRequestException(ErrorStatus.DUPLICATE_EMAIL.getMessage());
        }
    }

    @Transactional
    public void updateRefreshToken(String email, String refreshToken) {
        memberRepository.findByEmail(email)
                .ifPresent(member -> {
                    Member updatedMember = member.updateRefreshToken(refreshToken);
                    memberRepository.save(updatedMember);
                });
    }
}
