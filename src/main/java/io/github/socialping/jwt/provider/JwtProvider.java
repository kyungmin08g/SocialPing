package io.github.socialping.jwt.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${spring.jwt.secretKey}")
    public static String secretKey = "bd706b6c592d3778068f412f180ab015e918d07bcb58e123fa9958f8cc56c26f";

    @Value("${spring.jwt.access-token-expire-time}")
    private static long accessTokenExpireTime = 1209600;

    @Value("${spring.jwt.refresh-token-expire-time}")
    private static long refreshTokenExpireTime = 2628000;

    public static Key getDecoderSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8))));
    }

    public static String createAccessToken(String username, String role) {
        Claims claims = Jwts.claims();
        claims.put("username", username);
        claims.put("role", role);

        long expiration = accessTokenExpireTime * 1000;
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getDecoderSecretKey())
                .compact();
    }

    public static String createRefreshToken(String username, String role) {
        Claims claims = Jwts.claims();
        claims.put("username", username);
        claims.put("role", role);

        long expiration = refreshTokenExpireTime * 1000;
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getDecoderSecretKey())
                .compact();
    }

    public static String getUserName(String token) {
        return Jwts.parserBuilder().setSigningKey(getDecoderSecretKey()).build().parseClaimsJws(token).getBody().get("username", String.class);
    }

    public static String getRole(String token) {
        return Jwts.parserBuilder().setSigningKey(getDecoderSecretKey()).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getDecoderSecretKey()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("유효하지 않은 JWT 토큰: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료 된 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 문자열이 비어 있음: {}", e.getMessage());
        }

        return false;
    }

}
