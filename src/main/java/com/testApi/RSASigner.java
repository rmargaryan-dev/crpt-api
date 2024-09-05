package com.testApi;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class RSASigner {

    public static String signData(String data, PrivateKey privateKey) throws Exception {
        Signature rsa = Signature.getInstance("SHA256withRSA");
        rsa.initSign(privateKey);
        rsa.update(data.getBytes());

        byte[] signatureBytes = rsa.sign();

        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    public static PrivateKey getPrivateKeyFromBase64(String base64PrivateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public static void main(String[] args) {
        try {
            // Code that might throw an exception
            String result = riskyOperation();
            System.out.println("Result: " + result);

        } catch (Exception e) {
            // Handle the exception
            System.err.println("An error occurred: " + e.getMessage());
        }

        // Code that should run regardless of whether an exception occurred
        System.out.println("Continuing execution...");
    }

    private static String riskyOperation() throws Exception {
        // This is just an example; replace with your actual logic
        throw new Exception("This is a simulated exception");
    }
}
