package io.getmedusa.hydra.security;

import java.net.InetAddress;

public class IPUtils {

    private IPUtils() {}

    public static String getLocalIP() {
        try {
            String ip = System.getenv("SELF_POD_IP");
            if (ip == null) {
                return System.getProperty("SELF_POD_IP", InetAddress.getLocalHost().getHostAddress());
            } else {
                return ip;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
