package io.getmedusa.hydra.boot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getmedusa.hydra.model.ClientNode;
import io.getmedusa.hydra.model.ClientRoute;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.EcPublicJwk;
import io.jsonwebtoken.security.Jwks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PreDestroy;
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
@DependsOn("kubernetesService")
public class RegistrationStarter {

    private final KubernetesService kubernetesService;

    @Value("${spring.application.name:}")
    private String appName;

    @Value("${hydra.client.port:${server.port:8080}}")
    private Integer availableAtPort;

    private static boolean isRegistered = false;

    private final RequestMappingHandlerMapping handlerMapping;

    public RegistrationStarter(RequestMappingHandlerMapping handlerMapping, KubernetesService kubernetesService) {
        this.handlerMapping = handlerMapping;
        this.kubernetesService = kubernetesService;
    }

    static final KeyPair keyPair = Jwts.SIG.ES256.keyPair().build();

    private void registerAtHydra() {
        Set<String> ips = kubernetesService.getIPs();
        for(String ip : ips) {
            ClientNode self = buildClientNode();
            String hydraLocUrl = buildRegistrationUrl(ip);
            applyToHydra(hydraLocUrl, self);
        }
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    public void healthCheckWithHydra() {
        if(isRegistered) {
            System.out.println("health check to hydra - still alive!");
        } else {
            registerAtHydra();
        }
    }

    private static void applyToHydra(final String url, final ClientNode self) {
        applyToHydra(url, self, 0);
    }

    private static void applyToHydra(final String url, final ClientNode self, int retryAttempts) {
        if (retryAttempts > 3) { return; }
        try {
            final RestClient restClient = RestClient.create();
            ResponseEntity<String> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(self)
                    .retrieve()
                    .toEntity(String.class);
            if(!response.getStatusCode().is2xxSuccessful()) {
                throw new HttpClientErrorException(response.getStatusCode());
            } else {
                isRegistered = true;
                System.out.println("Registered w/ Hydra at " + url);
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            exponentialBackoff(retryAttempts);
            applyToHydra(url, self, retryAttempts + 1);
        }
    }

    private static void exponentialBackoff(int retryAttempts) {
        long backoffTime = Math.min((long) Math.pow(2, retryAttempts), 10);
        System.out.println("Retrying in " + backoffTime + " seconds...");

        try {
            Thread.sleep(backoffTime * 1000); // Convert seconds to milliseconds
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted while sleeping.");
        }
    }

    @PreDestroy
    public void deregisterGracefully() {
        //isActive = false;
        //if(!"none".equals(hydraLocationProperty)) {
            //String hydraLocUrl = buildDeregistrationUrl("");
            //ClientNode self = buildClientNode();
           // applyToHydra(hydraLocationProperty, self);
        //}
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
            return InetAddress.getLocalHost().getHostAddress() + ":" + availableAtPort;
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

    private String buildRegistrationUrl(String ip) {
        return ("http://" + ip + "/register").replace("//register", "/register");
    }

    private String buildDeregistrationUrl(String ip) {
        return ("http://" + ip + "/deregister").replace("//deregister", "/deregister");
    }

    static PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public static PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

}