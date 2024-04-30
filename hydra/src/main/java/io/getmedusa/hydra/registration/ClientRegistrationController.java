package io.getmedusa.hydra.registration;

import io.getmedusa.hydra.registration.model.ClientNode;
import io.getmedusa.hydra.routing.DynamicRouteProvider;
import io.getmedusa.hydra.security.InternalCallsOnly;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
This controller receives incoming registrations.
That means a connection to Hydra has been established from one of the client UI nodes.
*/
@RestController
public class ClientRegistrationController {

    private final DynamicRouteProvider dynamicRouteProvider;

    public ClientRegistrationController(DynamicRouteProvider dynamicRouteProvider) {
        this.dynamicRouteProvider = dynamicRouteProvider;
    }

    @PostMapping("/register")
    public Map<String, String> registerClient(ServerHttpRequest request, @RequestBody ClientNode clientNode) {
        InternalCallsOnly.ensure(request);
        dynamicRouteProvider.register(clientNode);
        dynamicRouteProvider.reload();
        return Map.of("status", "ok");
    }

    @PostMapping("/deregister")
    public Map<String, String> deregisterClient(ServerHttpRequest request, @RequestBody ClientNode clientNode) {
        InternalCallsOnly.ensure(request);
        dynamicRouteProvider.deregister(clientNode);
        dynamicRouteProvider.reload();
        return Map.of("status", "ok");
    }
}