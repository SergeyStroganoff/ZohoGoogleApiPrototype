package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * GoogleTokenManager is a class that manages the OAuth2 access token for Google APIs.
 * It handles the refresh token flow and provides a method to get the current access token.
 */
public class GoogleTokenManager {

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;

    private final AtomicReference<String> accessToken = new AtomicReference<>(null);
    private final AtomicReference<Instant> accessTokenExpiry = new AtomicReference<>(Instant.EPOCH); // Древняя дата

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleTokenManager(String clientId, String clientSecret, String refreshToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() throws Exception {
        Instant now = Instant.now();

        if (accessToken.get() == null || now.isAfter(accessTokenExpiry.get().minusSeconds(60))) {
            refreshAccessToken();
        }

        return accessToken.get();
    }

    private void refreshAccessToken() throws Exception {
        String body = String.format(
                "client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                clientId, clientSecret, refreshToken
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to refresh access token: " + response.body());
        }

        Map<String, Object> json = objectMapper.readValue(response.body(), Map.class);

        String newAccessToken = (String) json.get("access_token");
        int expiresInSeconds = (Integer) json.get("expires_in");

        accessToken.set(newAccessToken);
        accessTokenExpiry.set(Instant.now().plusSeconds(expiresInSeconds));

        System.out.println("Access token refreshed successfully.");
    }
}

