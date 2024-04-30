package io.getmedusa.hydra.security;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;

public class InternalCallsOnly {

    private InternalCallsOnly() {}

    public static void ensure(ServerHttpRequest request) {
        if (request != null && request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            final String incomingIP = request.getRemoteAddress().getAddress().getHostAddress();
            final String selfIP = IPUtils.getLocalIP();

            if(areInSameNetwork(selfIP, incomingIP)) {
                return;
            }
        }

        throw new SecurityException("Call origin to this endpoint must be within same network");
    }

    private static boolean areInSameNetwork(String selfIP, String incomingClientIP) {
        try {
            InetAddress ipAddress1 = InetAddress.getByName(selfIP);
            InetAddress ipAddress2 = InetAddress.getByName(incomingClientIP);

            byte[] networkPrefix1 = getNetworkPrefix(ipAddress1, 24);
            byte[] networkPrefix2 = getNetworkPrefix(ipAddress2, 24);

            return compareArrays(networkPrefix1, networkPrefix2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] getNetworkPrefix(InetAddress ipAddress, int prefixLength) {
        byte[] addressBytes = ipAddress.getAddress();
        byte[] networkPrefix = new byte[addressBytes.length];

        // Calculate the number of full bytes in the prefix
        int fullBytes = prefixLength / 8;

        // Copy the full bytes from the address
        System.arraycopy(addressBytes, 0, networkPrefix, 0, fullBytes);

        // Calculate the remaining bits in the prefix
        int remainingBits = prefixLength % 8;
        if (remainingBits > 0) {
            // Calculate the mask for the remaining bits
            int mask = (0xFF << (8 - remainingBits)) & 0xFF;
            // Apply the mask to the next byte
            networkPrefix[fullBytes] = (byte) (addressBytes[fullBytes] & mask);
        }

        return networkPrefix;
    }

    // Method to compare two byte arrays
    private static boolean compareArrays(byte[] array1, byte[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

}