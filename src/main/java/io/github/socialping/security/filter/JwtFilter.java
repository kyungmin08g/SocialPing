package io.github.socialping.security.filter;

import io.github.socialping.jwt.entity.RefreshTokenEntity;
import io.github.socialping.jwt.provider.JwtProvider;
import io.github.socialping.jwt.repository.RefreshTokenRepository;
import io.github.socialping.security.response.FacebookResponse;
import io.github.socialping.security.user.OAuth2FacebookUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository repository;

    public JwtFilter(JwtProvider jwtProvider, RefreshTokenRepository repository) {
        this.jwtProvider = jwtProvider;
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie accessToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) if (cookie.getName().equals("AT")) accessToken = cookie;

        if (accessToken != null) {
            String name = jwtProvider.getUserName(accessToken.getValue());
            String role = jwtProvider.getRole(accessToken.getValue());
            String userId = jwtProvider.getUserId(accessToken.getValue());

            FacebookResponse facebookResponse = FacebookResponse.builder()
                    .provider("facebook")
                    .name(name)
                    .userId(userId)
                    .build();

            // access token의 유효시간 체크
            if (jwtProvider.validateToken(accessToken.getValue())) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(new OAuth2FacebookUser(facebookResponse), null, List.of(new SimpleGrantedAuthority(role)))
                );
            } else {
                // access token이 없을 경우 refresh token이 있는지 체크
                RefreshTokenEntity refreshTokenEntity = repository.findById(jwtProvider.getUserName(accessToken.getValue())).orElse(null);

                if (refreshTokenEntity != null) {
                    String refreshToken = refreshTokenEntity.getRefresh_token();
                    if (jwtProvider.validateToken(refreshToken)) {
                        accessTokenIssue(new OAuth2FacebookUser(facebookResponse), role, response);
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(new OAuth2FacebookUser(facebookResponse), null, List.of(new SimpleGrantedAuthority(role)))
                        );
                        log.info("\u001B[32m엑세스 토큰 다시 발급\u001B[0m");
                    } else {
                        repository.deleteById(refreshTokenEntity.getName());
                        accessToken.setMaxAge(0);
                        response.addCookie(accessToken);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void accessTokenIssue(OAuth2FacebookUser user, String role, HttpServletResponse response) {
        String accessToken = jwtProvider.createAccessToken(user.getName(), user.getUserId(), role);
        Cookie cookie = new Cookie("AT", accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(cookie);
    }

}
