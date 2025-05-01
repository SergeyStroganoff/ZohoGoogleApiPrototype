package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ZohoService {

    private static final Logger logger = LoggerFactory.getLogger(ZohoService.class);

    private static final String ZOHO_API_URL = "https://books.zoho.com/api/v3/contacts";
    private static final String ORGANIZATION_ID = "YOUR_ORG_ID";
    private static final String AUTH_TOKEN = "YOUR_ACCESS_TOKEN";
    private final HttpClient httpClient;

    public ZohoService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void addNewContact(String contactName, String email) {
        String requestBody = String.format(
                "{\"contact_name\":\"%s\", \"email\":\"%s\"}",
                contactName, email
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ZOHO_API_URL + "?organization_id=" + ORGANIZATION_ID))
                .header("Authorization", "Zoho-oauthtoken " + AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Контакт успешно добавлен! Ответ: " + response.body());
            } else {
                System.err.println("Ошибка при добавлении контакта. Код: " + response.statusCode());
                System.err.println("Ответ сервера: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
