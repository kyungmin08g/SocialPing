package io.github.socialping.security.service;

import io.github.socialping.security.response.FacebookResponse;
import io.github.socialping.security.user.OAuth2FacebookUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OAuth2Service extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        FacebookResponse response = FacebookResponse.builder()
                .provider("facebook")
                .providerId(oAuth2User.getAttributes().get("id").toString())
                .name(oAuth2User.getAttributes().get("name").toString())
                .email(oAuth2User.getAttributes().get("email").toString())
                .build();

        log.info("\u001B[34maccessToken: {}\u001B[0m", userRequest.getAccessToken().getTokenValue());
        log.info("\u001B[34mFacebookUser(provider: {}, name: {}, email: {})\u001B[0m",
                userRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes().get("name").toString(),
                oAuth2User.getAttributes().get("email").toString()
        );

        return new OAuth2FacebookUser(response);
    }
}
