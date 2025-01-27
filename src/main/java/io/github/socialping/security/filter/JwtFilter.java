package io.github.socialping.security.filter;

import io.github.socialping.jwt.entity.RefreshTokenEntity;
import io.github.socialping.jwt.provider.JwtProvider;
import io.github.socialping.jwt.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final RefreshTokenRepository repository;

    @Autowired
    public JwtFilter(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie accessToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) if (cookie.getName().equals("AT")) accessToken = cookie;

        if (accessToken != null) {
            String name = JwtProvider.getUserName(accessToken.getValue());
            String role = JwtProvider.getRole(accessToken.getValue());

            // access token의 유효시간 체크
            if (JwtProvider.validateToken(accessToken.getValue())) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(name, null, List.of(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // access token이 없을 경우 refresh token이 있는지 체크
                RefreshTokenEntity refreshTokenEntity = repository.findById(JwtProvider.getUserName(accessToken.getValue())).orElse(null);

                if (refreshTokenEntity != null) {
                    String refreshToken = refreshTokenEntity.getRefresh_token();
                    if (JwtProvider.validateToken(refreshToken)) {
                        accessTokenIssue(name, role, response);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(name, null, List.of(new SimpleGrantedAuthority(role)));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
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

    private void accessTokenIssue(String name, String role, HttpServletResponse response) {
        String accessToken = JwtProvider.createAccessToken(name, role);
        Cookie cookie = new Cookie("AT", accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(cookie);
    }

}
