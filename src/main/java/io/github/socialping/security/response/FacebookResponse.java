package io.github.socialping.security.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class FacebookResponse {
    public String provider;
    public String providerId;
    public String name;
    public String email;
}
