package com.testApi;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final Semaphore semaphore;
    private final HttpClient httpClient;
    private String token;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        this.httpClient = HttpClient.newHttpClient();

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                this.token = "";
            }
            properties.load(input);

            this.token = properties.getProperty("auth.token");

        } catch (IOException ex) {
            System.out.println("Sorry, unable to find application.properties");
        }

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

    public void createDocument(Object document, String signature) throws InterruptedException {
        semaphore.acquire();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestBody = new HashMap<>();

            requestBody.put("product_document", document);
            requestBody.put("document_format", "MANUAL");
            requestBody.put("signature", signature);

            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to create document: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error processing document", e);
        } finally {
            semaphore.release();
        }
    }
}
