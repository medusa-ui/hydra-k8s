package io.getmedusa.hydra.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class LoginRedirectFilterFactory extends AbstractGatewayFilterFactory<LoginRedirectFilterFactory.Config> {

    private static final String LOGIN_PAGE_URL = "/login";

    public LoginRedirectFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            HttpStatusCode statusCode = response.getStatusCode();

            if (statusCode == HttpStatus.FORBIDDEN || hasLoginLocationHeader(response.getHeaders())) {
                URI loginPageUri = UriComponentsBuilder.fromUriString(LOGIN_PAGE_URL)
                        .queryParam("ref", exchange.getRequest().getURI().getPath())
                        .build().toUri();
                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.getHeaders().setLocation(loginPageUri);
                return response.setComplete();
            }
            return Mono.empty();
        }));
    }

    private boolean hasLoginLocationHeader(HttpHeaders headers) {
        URI location = headers.getLocation();
        return null!= location && location.toString().endsWith(LOGIN_PAGE_URL);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return new ArrayList<>();
    }

    public static class Config {
    }
}