package io.getmedusa.hydra.jwt;

import io.getmedusa.hydra.registration.model.ClientNode;
import io.getmedusa.hydra.security.model.HydraUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.AeadAlgorithm;
import io.jsonwebtoken.security.KeyAlgorithm;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

public class JWEGenerator {

    private JWEGenerator() {}

    public static String build(HydraUser user, ClientNode clientNode) {
        if(clientNode == null) return null;

        //you should probably be able to customize this, but you have put the same on UI and hydra
        final KeyAlgorithm<PublicKey, PrivateKey> alg = Jwts.KEY.ECDH_ES_A128KW;
        final AeadAlgorithm enc = Jwts.ENC.A128GCM;

        return Jwts.builder()
                .audience().add(clientNode.name())
                .and()
                .claims()
                    .add("u",  user.getUsername())
                    .add("r", user.getRoles())
                .and()
                .expiration(new Date(new Date().getTime() + 60 * 1000L))
                .encryptWith(clientNode.constructKey(), alg, enc)
                //.signWith(clientNode.constructKey())
                .compact();
    }
}
