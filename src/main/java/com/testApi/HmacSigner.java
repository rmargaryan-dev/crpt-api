package com.testApi;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class HmacSigner {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    public static String sign(String data, String secretKey) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);  // Return Base64 encoded signature
    }

    public static void main(String[] args) {
        try {
            // The JSON payload you want to sign
            String payload = "{\"uuid\":\"04b84ba3-76f4-4195-a85b-42c527198578\",\"data\":\"PFLZRTXRPSLYHHQQPZWQWHSGYSDKGE\"}";

            // The secret key used for signing (replace this with your actual secret key)
            String secretKey = "your-secret-key";

            // Sign the payload
            String signature = sign(payload, secretKey);

            // Print the signed payload
            System.out.println("Signed Payload: " + signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
