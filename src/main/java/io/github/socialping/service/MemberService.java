package io.github.socialping.service;

import io.github.socialping.dto.JoinDto;
import io.github.socialping.dto.MemberDto;
import io.github.socialping.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void join(SecurityContext context, JoinDto dto) {
        String username = context.getAuthentication().getName();
        String uuid = username.substring(0, username.indexOf("_"));
        String name = username.substring(username.indexOf("_") + 1);

        String role = null;
        for (GrantedAuthority authority : context.getAuthentication().getAuthorities()) { role = authority.getAuthority(); }

        MemberDto memberDto = MemberDto.builder()
                .uuid(uuid)
                .username(name)
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .business(dto.getBusiness())
                .role(role)
                .build();

        try {
            memberRepository.save(memberDto.toEntity());
        } catch (Exception e) {
            log.error("\u001B[31m회원 저장하는 로직에서 예외: {}\u001B[0m", e.getMessage());
        }
    }
}
