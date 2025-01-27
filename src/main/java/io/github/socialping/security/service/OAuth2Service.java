package io.github.socialping.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.socialping.security.response.FacebookResponse;
import io.github.socialping.security.user.OAuth2FacebookUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class OAuth2Service extends DefaultOAuth2UserService {

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        FacebookResponse response = FacebookResponse.builder()
                .provider("facebook")
                .providerId(oAuth2User.getAttributes().get("id").toString())
                .name(oAuth2User.getAttributes().get("name").toString())
                .email(oAuth2User.getAttributes().get("email").toString())
                .build();

        String accessToken = getAccessToken(userRequest);

        log.info("\u001B[32m긴 유효시간을 가진 액세스 토큰: {}\u001B[0m", accessToken);
        log.info("\u001B[34mFacebookUser(provider: {}, name: {}, email: {})\u001B[0m",
                userRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes().get("name").toString(),
                oAuth2User.getAttributes().get("email").toString()
        );

        return new OAuth2FacebookUser(response);
    }

    // RestClient를 이용하여 긴 유효시간을 가진 액세스 토큰 얻기
    private String getAccessToken(OAuth2UserRequest userRequest) {
        RestClient restClient = RestClient.create("https://graph.facebook.com");
        String longAccessToken = restClient.get().uri(uriBuilder -> uriBuilder.path("/v17.0/oauth/access_token")
                .queryParam("grant_type", "fb_exchange_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("fb_exchange_token", userRequest.getAccessToken().getTokenValue())
                .build()
        ).retrieve().body(String.class);

        try {
            return objectMapper.readTree(longAccessToken).get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
