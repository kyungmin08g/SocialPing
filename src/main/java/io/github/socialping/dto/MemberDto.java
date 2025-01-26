package io.github.socialping.dto;

import io.github.socialping.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class MemberDto {

    private String uuid;
    private String username;
    private String email;
    private String phoneNumber;
    private String business;
    private String role;

    public MemberEntity toEntity() {
        return MemberEntity.builder()
                .uuid(uuid)
                .username(username)
                .email(email)
                .phoneNumber(phoneNumber)
                .business(business)
                .role(role)
                .build();
    }
}
