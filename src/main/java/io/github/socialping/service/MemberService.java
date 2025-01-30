package io.github.socialping.service;

import io.github.socialping.dto.JoinDto;
import io.github.socialping.entity.MemberEntity;
import io.github.socialping.repository.MemberRepository;
import io.github.socialping.security.user.OAuth2FacebookUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void join(SecurityContext context, JoinDto dto) {
        OAuth2FacebookUser user = (OAuth2FacebookUser) context.getAuthentication().getPrincipal();

        String role = null;
        for (GrantedAuthority authority : context.getAuthentication().getAuthorities()) { role = authority.getAuthority(); }

        try {
            MemberEntity memberEntity = memberRepository.findById(user.getUserId()).orElse(null);
            if (memberEntity != null) {
                memberEntity.setEmail(dto.getEmail());
                memberEntity.setPhoneNumber(dto.getPhoneNumber());
                memberEntity.setBusiness(dto.getBusiness());
                memberEntity.setRole(role);
                memberRepository.save(memberEntity);
            }
        } catch (Exception e) {
            log.error("\u001B[31m회원 저장하는 로직에서 예외: {}\u001B[0m", e.getMessage());
        }
    }
}
