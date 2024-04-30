package io.getmedusa.hydra.boot;

import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.security.PublicKey;

class RegistrationStarterTest {

    private final RegistrationStarter service = new RegistrationStarter(Mockito.mock(RequestMappingHandlerMapping.class), Mockito.mock(KubernetesService.class));

    @Test
    void testPublicKeyToString() {
        final String publicKey = service.publicKeyToString();
        System.out.println(publicKey);
        Assertions.assertNotNull(publicKey);
        Assertions.assertFalse(publicKey.isBlank());
        Assertions.assertEquals(constructedKey(publicKey), service.getPublicKey());
    }

    private PublicKey constructedKey(String publicKey) {
        Jwk<?> parsed = Jwks.parser().build().parse(publicKey);
        return (PublicKey) parsed.toKey();
    }
}
