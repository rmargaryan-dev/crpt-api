package com.testApi;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private static final Properties PROPERTIES = getProperties("application.properties");
    // getting token and url from application.properties
    private static final String API_URL = PROPERTIES.getProperty("api.url");
    private static final String TOKEN = PROPERTIES.getProperty("auth.token");
    private final Semaphore semaphore;
    private final HttpClient httpClient;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        this.httpClient = HttpClient.newHttpClient();

        long intervalMillis = timeUnit.toMillis(1);
        Thread refillThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(intervalMillis);
                    semaphore.release(requestLimit - semaphore.availablePermits());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        refillThread.setDaemon(true);
        refillThread.start();
    }

    public static Properties getProperties(String path) {
        Properties properties = new Properties();
        try (InputStream input = CrptApi.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return new Properties();
            }
            properties.load(input);

            return properties;

        } catch (IOException ex) {
            System.out.println("Sorry, unable to find application.properties");
            return new Properties();
        }
    }

    public void createDocument(Object document, String signature) throws InterruptedException {
        semaphore.acquire();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestBody = new HashMap<>();
            Map documentMap = objectMapper.convertValue(document, Map.class);

            requestBody.put("product_document", encodeObjectToBase64(document));
            requestBody.put("document_format", DocumentType.MANUAL.getIdentifier());
            requestBody.put("type", documentMap.get("doc_type"));
            // signature is already base64
            requestBody.put("signature", signature);

            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("Failed to create document:\n" + response.body());
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error processing document\n" + e.getMessage());
        } finally {
            semaphore.release();
        }
    }

    public String encodeObjectToBase64(Object obj) {
        String base64EncodedString = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();

            byte[] objectBytes = byteArrayOutputStream.toByteArray();
            base64EncodedString = Base64.getEncoder().encodeToString(objectBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return base64EncodedString;
    }

    private enum DocumentType {
        MANUAL("MANUAL"),
        XML("XML"),
        CSV("CSV");

        private final String identifier;

        DocumentType(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }
    }
}
