package com.testApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class CrptRequestTest {

    private static final String API_URL = "https://ismp.crpt.ru/api/v3/auth/cert/key";

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> data = new HashMap<>();

        data.put("uuid", "04b84ba3-76f4-4195-a85b-42c527198578");
        data.put("data", "PFLZRTXRPSLYHHQQPZWQWHSGYSDKGE");

        String jsonRequestBody = null;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            System.out.println("no no no ");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
//                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response   ----------     " + response.toString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to create document: " + response.body());
        }

    }
}
