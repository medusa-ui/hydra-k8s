package io.getmedusa.hydra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import static org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.SAME_ORIGIN;
import static org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.mode(SAMEORIGIN))
                        .referrerPolicy(spec -> spec.policy(SAME_ORIGIN)))
                .authorizeExchange(spec -> spec.anyExchange().permitAll())
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(new HydraAuthSuccessHandler()))
                .csrf(csrfSpec -> csrfSpec.requireCsrfProtectionMatcher(csrfRequiredMatcher()))
                .build();
    }

    private static ServerWebExchangeMatcher csrfRequiredMatcher() {
        return exchange -> ServerWebExchangeMatchers.pathMatchers("*/login").matches(exchange);
    }
}