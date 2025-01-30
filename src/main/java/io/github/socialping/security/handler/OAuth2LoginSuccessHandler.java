package io.github.socialping.security.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.socialping.jwt.entity.RefreshTokenEntity;
import io.github.socialping.jwt.provider.JwtProvider;
import io.github.socialping.jwt.repository.RefreshTokenRepository;
import io.github.socialping.security.user.OAuth2FacebookUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RefreshTokenRepository repository;

    @Autowired
    public OAuth2LoginSuccessHandler(JwtProvider jwtProvider, RefreshTokenRepository repository) {
        this.jwtProvider = jwtProvider;
        this.repository = repository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("\u001B[32mFacebook login success!\u001B[0m");

        OAuth2FacebookUser facebookUser = (OAuth2FacebookUser) authentication.getPrincipal();
        System.out.println(facebookUser.toString());

        String principal = objectMapper.writeValueAsString(authentication.getPrincipal());
        JsonNode json = objectMapper.readTree(principal);
        String authority = null;
        for (JsonNode authorities : json.get("authorities")) {
            authority = authorities.get("authority").asText();
        }

        // SecurityContextHolder에 로그인한 유저 정보 저장
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(facebookUser, null, List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        accessTokenAndRefreshTokenIssue(facebookUser, authority, response);
    }

    private void accessTokenAndRefreshTokenIssue(OAuth2FacebookUser user, String authority, HttpServletResponse response) throws IOException {
        String accessToken = jwtProvider.createAccessToken(user.getName(), user.getUserId(), authority);
        String refreshToken = jwtProvider.createRefreshToken(user.getName(), user.getUserId(), authority);
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder().name(user.getName()).refresh_token(refreshToken).build();
        repository.save(refreshTokenEntity);

        Cookie cookie = new Cookie("AT", accessToken);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setPath("/");
        response.addCookie(cookie);
        response.sendRedirect("/signup");
    }
}
