package io.getmedusa.hydra.model;

import java.util.Set;

public record ClientNode(String name, String uri, String publicKey, Set<ClientRoute> routes) {

    @Override
    public String toString() {
        return "ClientNode{" +
                "name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                ", routes=" + routes +
                '}';
    }
}
