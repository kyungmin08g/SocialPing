package io.github.socialping.service;

import io.github.socialping.dto.JoinDto;
import io.github.socialping.dto.MemberDto;
import io.github.socialping.entity.MemberEntity;
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

    public boolean join(SecurityContext context, JoinDto dto) {
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
        System.out.println(memberDto.toString());

        try {
            memberRepository.save(memberDto.toEntity());
            return true;
        } catch (Exception e) {
            log.error("\u001B[31mError: {}\u001B[0m", e.getMessage());
            return false;
        }
    }
}
