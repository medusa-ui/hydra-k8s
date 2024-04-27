package io.getmedusa.hydra.boot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getmedusa.hydra.model.ClientNode;
import io.getmedusa.hydra.model.ClientRoute;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.EcPublicJwk;
import io.jsonwebtoken.security.Jwks;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
This class registers the UI service with Hydra based on the 'hydra.location' property.
By default, the location to register would be http://localhost:9988/register.
Keep in mind that the property could be ending in a / or without one, either should work
 */
@Component
public class RegistrationStarter implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${hydra.location:none}")
    private String hydraLocationProperty;

    @Value("${spring.application.name:}")
    private String appName;

    @Value("${hydra.client.port:${server.port:8080}}")
    private Integer serverPort;

    private final RequestMappingHandlerMapping handlerMapping;

    public RegistrationStarter(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    static final KeyPair keyPair = Jwts.SIG.ES256.keyPair().build();

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("Registering at : " + hydraLocationProperty);
        if(!"none".equals(hydraLocationProperty)) {
            final RestClient restClient = RestClient.create();

            String hydraLocUrl = buildRegistrationUrl();
            ClientNode self = buildClientNode();

            //TODO keep retrying if this fails or when you lose connection
            try {
                ResponseEntity<String> response = restClient.post()
                        .uri(hydraLocUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(self)
                        .retrieve()
                        .toEntity(String.class);
                System.out.println(response);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                //just retry
            }
        }
    }

    @PreDestroy
    public void deregisterGracefully() {
        if(!"none".equals(hydraLocationProperty)) {
            final RestClient restClient = RestClient.create();

            String hydraLocUrl = buildDeregistrationUrl();
            ClientNode self = buildClientNode();

            try {
                ResponseEntity<String> response = restClient.post()
                        .uri(hydraLocUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(self)
                        .retrieve()
                        .toEntity(String.class);
                System.out.println(response);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                //just retry
            }
        }
    }

    private ClientNode buildClientNode() {
        return new ClientNode(appName,
                getCurrentIP(),
                publicKeyToString(),
                findRoutesWithinThisApplication());
    }

    String publicKeyToString() {
        ECPublicKey key = (ECPublicKey) RegistrationStarter.getPublicKey();
        EcPublicJwk jwk = Jwks.builder().key(key).idFromThumbprint().build();
        try {
            return new ObjectMapper().writeValueAsString(jwk);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCurrentIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<ClientRoute> findRoutesWithinThisApplication() {
        Set<ClientRoute> routes = new HashSet<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        for (RequestMappingInfo mappingInfo : handlerMethods.keySet()) {
            if(!mappingInfo.getMethodsCondition().getMethods().isEmpty()) {
                for(RequestMethod method : mappingInfo.getMethodsCondition().getMethods()) {
                    for(String path : mappingInfo.getPatternValues()) {
                        ClientRoute route = new ClientRoute(path, method.asHttpMethod().name());
                        routes.add(route);
                    }
                }
            }
        }
        return routes;
    }

    private String buildRegistrationUrl() {
        return (hydraLocationProperty + "/register").replace("//register", "/register");
    }

    private String buildDeregistrationUrl() {
        return (hydraLocationProperty + "/deregister").replace("//deregister", "/deregister");
    }

    static PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public static PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

}