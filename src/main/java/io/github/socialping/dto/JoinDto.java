package io.github.socialping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@Builder
public class JoinDto {
    public String email;
    public String phoneNumber;
    public String business;
}
