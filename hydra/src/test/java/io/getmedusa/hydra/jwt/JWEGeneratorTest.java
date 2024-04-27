package io.getmedusa.hydra.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.AeadAlgorithm;
import io.jsonwebtoken.security.KeyAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;

class JWEGeneratorTest {

    @Test
    void generateKey() {
        //you should probably be able to customize this, but you have put the same on UI and hydra
        KeyPair pair = Jwts.SIG.ES256.keyPair().build();
        KeyAlgorithm<PublicKey, PrivateKey> alg = Jwts.KEY.ECDH_ES_A128KW;
        AeadAlgorithm enc = Jwts.ENC.A128GCM;

        String message = "Live long and prosper.";

        String jwe = Jwts.builder()
                .audience().add("Alice")
                .and()
                .claims().add("p", message)
                .and()
                .encryptWith(pair.getPublic(), alg, enc)
                .compact();

        System.out.println(jwe.length());

        Claims payload = Jwts.parser()
                .decryptWith(pair.getPrivate())
                .build().parseEncryptedClaims(jwe).getPayload();
        Set<String> audience = payload.getAudience();

        Assertions.assertEquals("Alice", audience.toArray()[0]);
        Assertions.assertEquals(message, payload.get("p"));
    }

}
