package io.getmedusa.hydra.security;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.net.InetSocketAddress;

class InternalCallsOnlyTest {

    @BeforeEach
    void setUp() {
        System.setProperty("SELF_POD_IP", "192.168.1.100");
    }

    @Test
    void testSameNetWorkIsOK() {
        try {
            InternalCallsOnly.ensure(buildReq("192.168.1.101"));
            Assertions.assertTrue(true);
        } catch (SecurityException e) {
            Assertions.fail("Should pass");
        }
    }

    private static @NotNull MockServerHttpRequest buildReq(String incomingClientIP) {
        return MockServerHttpRequest
                .method(HttpMethod.GET, "/your-endpoint")
                .remoteAddress(new InetSocketAddress(incomingClientIP, 80))
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                .build();
    }

    @Test
    void testOtherNetWorkIsNOK() {
        try {
            InternalCallsOnly.ensure(buildReq("193.168.1.101"));
            Assertions.fail("Should NOT pass");
        } catch (SecurityException e) {
            Assertions.assertTrue(true);
        }
    }

}
