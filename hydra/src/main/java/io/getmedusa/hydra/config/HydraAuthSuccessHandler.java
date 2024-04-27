package io.getmedusa.hydra.config;

import io.getmedusa.hydra.jwt.JWEGenerator;
import io.getmedusa.hydra.registration.model.ClientNode;
import io.getmedusa.hydra.routing.DynamicRouteProvider;
import io.getmedusa.hydra.security.model.HydraUser;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;

public class HydraAuthSuccessHandler extends RedirectServerAuthenticationSuccessHandler {
    private static final List<String> DEFAULT = List.of("/");

    public HydraAuthSuccessHandler() {
        super();
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        final ServerWebExchange exchange = webFilterExchange.getExchange();

        final HydraUser hydraUser = (HydraUser) authentication.getPrincipal();
        final ServerHttpRequest request = exchange.getRequest();
        final ClientNode clientNode = DynamicRouteProvider.findClient(request);
        final String jweToken = JWEGenerator.build(hydraUser, clientNode);
        if(jweToken == null) {
            throw new SecurityException("Could not generate a JWE token");
        }

        exchange.getResponse().addCookie(
                ResponseCookie.from("HYDRA-SSO", jweToken)
                        .httpOnly(true)
                        .maxAge(Duration.ofMinutes(1))
                        .build());

        setLocationToReferer(exchange.getRequest().getQueryParams());

        return super.onAuthenticationSuccess(webFilterExchange, authentication);
    }

    private void setLocationToReferer(MultiValueMap<String, String> queryParams) {
        if (queryParams != null) {
            String referer = queryParams.getOrDefault("ref", DEFAULT).get(0);
            if(referer.contains("?")) {
                referer = referer.split("\\?")[0];
            }

            setLocation(URI.create(referer));
        }
    }
}
