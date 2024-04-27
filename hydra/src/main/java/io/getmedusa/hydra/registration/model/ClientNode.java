package io.getmedusa.hydra.registration.model;

import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;

import java.security.PublicKey;
import java.util.Set;

public record ClientNode(String name, String uri, String publicKey, Set<ClientRoute> routes) {

    public PublicKey constructKey() {
        Jwk<?> parsed = Jwks.parser().build().parse(publicKey);
        return (PublicKey) parsed.toKey();
    }

    @Override
    public String toString() {
        return "ClientNode{" +
                "name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                ", routes=" + routes +
                '}';
    }
}
