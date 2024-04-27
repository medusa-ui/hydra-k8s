package io.getmedusa.hydra.security;

import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class HydraSecurity extends AbstractHttpConfigurer<HydraSecurity, HttpSecurity> {

    @Override
    public void configure(HttpSecurity http) {
        http.addFilterBefore(new HydraJWEAuthFilter(expectedAudience), UsernamePasswordAuthenticationFilter.class);
    }

    public static HydraSecurity hydraSecurity(Environment environment) {
        return new HydraSecurity(environment);
    }

    final String expectedAudience;

    private HydraSecurity(Environment environment) {
        expectedAudience = environment.getProperty("spring.application.name", "none");
    }
}
