package io.getmedusa.hydra.registration.model;

public record ClientRoute(String route, String httpMethod) {

    @Override
    public String toString() {
        return "ClientRoute{" +
                "route='" + route + '\'' +
                ", httpMethod=" + httpMethod +
                '}';
    }
}
