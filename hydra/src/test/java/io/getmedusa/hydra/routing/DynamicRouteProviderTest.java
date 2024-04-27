package io.getmedusa.hydra.routing;

import io.getmedusa.hydra.registration.model.ClientNode;
import io.getmedusa.hydra.registration.model.ClientRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.util.Collections;

@SpringBootTest
class DynamicRouteProviderTest {

    @Autowired
    private DynamicRouteProvider dynamicRouteProvider;

    private final ClientNode clientNode = new ClientNode("TestNode", "localhost:8080", Collections.singleton(new ClientRoute("/test", "GET")));

    @BeforeEach
    void clear() {
        dynamicRouteProvider.clear();
    }


    @Test
    void testReloadRoutesUponRegistration() {
        dynamicRouteProvider.register(clientNode);
        dynamicRouteProvider.reload();

        StepVerifier.create(dynamicRouteProvider.getRoutes())
                .expectNextMatches(route ->
                        route.getUri().toString().equals("http://localhost:8080") && route.getPredicate().toString().contains("/test") && route.getPredicate().toString().contains("GET"))
                .expectComplete()
                .verify();
    }

    @Test
    void testReloadRoutesUponDeregistration() {
        dynamicRouteProvider.register(clientNode);
        dynamicRouteProvider.reload();

        dynamicRouteProvider.deregister(clientNode);
        dynamicRouteProvider.reload();

        StepVerifier.create(dynamicRouteProvider.getRoutes())
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    void testConcurrentModificationsToRegisteredNodes() {
        ClientNode newNode = new ClientNode("NewNode", "localhost:8081", Collections.singleton(new ClientRoute("/new", "POST")));

        dynamicRouteProvider.register(clientNode);
        dynamicRouteProvider.reload();

        dynamicRouteProvider.register(newNode);
        dynamicRouteProvider.reload();

        dynamicRouteProvider.deregister(clientNode);
        dynamicRouteProvider.reload();


        // Verify that the new node is registered and the initial node is deregistered
        StepVerifier.create(dynamicRouteProvider.getRoutes())
                .expectNextMatches(route -> route.getUri().toString().equals("http://localhost:8081") && route.getPredicate().toString().contains("/new") && route.getPredicate().toString().contains("POST"))
                .expectComplete()
                .verify();
    }
}
