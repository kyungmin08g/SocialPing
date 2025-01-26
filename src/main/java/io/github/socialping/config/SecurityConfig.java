package io.github.socialping.config;

import io.github.socialping.security.handler.OAuth2LoginFailureHandler;
import io.github.socialping.security.handler.OAuth2LoginSuccessHandler;
import io.github.socialping.security.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final OAuth2Service oAuth2Service;
    private final OAuth2LoginSuccessHandler successHandler;
    private final OAuth2LoginFailureHandler failureHandler;

    @Autowired
    public SecurityConfig(
        OAuth2Service oAuth2Service,
        OAuth2LoginSuccessHandler successHandler,
        OAuth2LoginFailureHandler failureHandler
    ) {
        this.oAuth2Service = oAuth2Service;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests((auth) -> {
           auth.requestMatchers("/", "/login", "/signup", "/css/**", "/images/**").permitAll()
                   .anyRequest().authenticated();
        });

        http.oauth2Login((oAuth) -> {
           oAuth.loginPage("/login");
           oAuth.userInfoEndpoint((userInfoEndpointConfig -> userInfoEndpointConfig.userService(oAuth2Service)));
           oAuth.successHandler(successHandler);
           oAuth.failureHandler(failureHandler);
        });

        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
