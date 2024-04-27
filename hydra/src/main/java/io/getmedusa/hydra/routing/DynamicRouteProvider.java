package io.getmedusa.hydra.routing;

import io.getmedusa.hydra.config.SecurityConfig;
import io.getmedusa.hydra.filter.LoginRedirectFilterFactory;
import io.getmedusa.hydra.registration.model.ClientNode;
import io.getmedusa.hydra.registration.model.ClientRoute;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides the gateway proxy routes. Usually, those are configured statically. For Hydra to work,
 * they need to be able to be set dynamically upon registration.
 */

@Component
public class DynamicRouteProvider extends CachingRouteLocator {

    private final RouteLocatorBuilder builder;
    private final SecurityConfig securityConfig;

    //important here that the Flux is not final, we specifically want to be able to reload this
    private Flux<Route> routeFlux = Flux.empty();

    private final GatewayFilter loginFilter;

    public DynamicRouteProvider(RouteLocatorBuilder builder, SecurityConfig securityConfig, LoginRedirectFilterFactory loginRedirectFilterFactory) {
        super(new NoopLocator());
        this.builder = builder;
        this.securityConfig = securityConfig;
        this.loginFilter = loginRedirectFilterFactory.apply(new LoginRedirectFilterFactory.Config());
    }

    public static boolean isAvailableRoute(String forwardToLocationAfterLogin) {
        if("/".equals(forwardToLocationAfterLogin)) {
            return true;
        }
        for(ClientNode node : nodes) {
            for(ClientRoute route : node.routes()) {
                if(route.route().equals(forwardToLocationAfterLogin)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static ClientNode findClient(ServerHttpRequest request) {
        final String path = request.getQueryParams().getFirst("ref");

        for(ClientNode node : nodes) {
            for(ClientRoute route : node.routes()) {
                if(route.route().equals(path)) {
                    return node;
                }
            }
        }
        return null;
    }

    @Bean
    @Primary
    public RouteLocator cachedCompositeRouteLocator(List<RouteLocator> routeLocators) {
        return this;
    }

    public void reload() {
        final RouteLocatorBuilder.Builder routeBuilder = this.builder.routes();
        final Set<ClientNode> copiedNodes = Set.copyOf(nodes); //this way this collection can change whenever, even when reloading
        for(ClientNode node : copiedNodes) {
            for(ClientRoute route : node.routes()) {
                System.out.println("Registering " + route);
                routeBuilder.route(route.route(), r -> r
                        .method(route.httpMethod())
                        .and()
                        .path(route.route())
                        .filters(spec -> spec.filter(loginFilter))
                        .uri("http://" + node.uri()));
            }
        }
        routeFlux = routeBuilder.build().getRoutes();
    }

    @Override
    public Flux<Route> getRoutes() {
        return routeFlux;
    }

    private final static Set<ClientNode> nodes = new HashSet<>();

    public void register(ClientNode clientNode) {
        nodes.add(clientNode);
    }

    public void deregister(ClientNode clientNode) {
        nodes.remove(clientNode);
    }

    public void clear() {
        nodes.clear();
        reload();
    }

    /**
     * Negating the use of RouteLocator, all our routes comes from the dynamic route holder instead.
     */
    static class NoopLocator implements RouteLocator {
        @Override
        public Flux<Route> getRoutes() {
            return Flux.empty();
        }
    }
}
