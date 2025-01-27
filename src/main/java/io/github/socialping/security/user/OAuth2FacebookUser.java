package io.github.socialping.security.user;

import io.github.socialping.security.response.FacebookResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OAuth2FacebookUser implements OAuth2User {

    private FacebookResponse response;

    @Override
    public Map<String, Object> getAttributes() { return null; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() { return "ROLE_USER"; }
        });
        return authorities;
    }

    @Override
    public String getName() { return UUID.randomUUID() + "_" + response.getName(); }
}
