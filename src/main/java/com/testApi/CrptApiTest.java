package com.testApi;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CrptApiTest {

    public static final String DOCUMENT_PATH = "documents/document.json";

    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 3);
        Object document;

        ExecutorService executor = Executors.newFixedThreadPool(5);
        try (InputStream inputStream = CrptApiTest.class.getClassLoader().getResourceAsStream(DOCUMENT_PATH)) {
            ObjectMapper objectMapper = new ObjectMapper();
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + DOCUMENT_PATH);
            }
            document = objectMapper.writeValueAsString(objectMapper.readValue(inputStream, Object.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        for (int i = 0; i < 12; i++) {
            executor.submit(() -> {
                try {
                    api.createDocument(document,"YIXJKGSCUGTXZABMAPJLHXJVYKQJUE");
                    System.out.println("Document created successfully by " + Thread.currentThread().getName());
                } catch (Exception e) {
                    System.err.println("Failed to create document: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
