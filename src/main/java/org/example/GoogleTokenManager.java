package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.Asserts;
import org.example.entity.AppCredentials;
import org.example.exception.AuthenticationError;
import org.example.utils.CredentialsRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;

/**
 * GoogleTokenManager is a class that manages the OAuth2 access token for Google APIs.
 * It handles the refresh token flow and provides a method to get the current access token.
 */
public class GoogleTokenManager {
    public static final String ACCESS_DENIED_INVALID_OR_MISSING_CREDENTIALS = "Access denied: Invalid or missing credentials";
    /**
     * The AppCredentials object contains the client ID, client secret, access token, and refresh token.
     */
    private AppCredentials credentials;
    private final CredentialsRetriever credentialsRetriever;
    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenManager.class);
    // private final AtomicReference<String> newAccessToken = new AtomicReference<>(null);
    // = new AtomicReference<>(Instant.EPOCH); // Древняя дата
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_RETRIES = 3;

    public GoogleTokenManager(CredentialsRetriever credentialsRetriever) {
        this.credentialsRetriever = credentialsRetriever;
    }

    /**
     * Get the new access token. If the current access token is expired or about to expire,
     * it refreshes the token using the refresh token.
     */
    public String getNewAccessToken() throws IOException {
        if (credentials == null) {
            credentials = credentialsRetriever.readCredentials();
        }
        Instant now = Instant.now();
        if (now.isAfter(credentials.getAccessTokenExpiry().minusSeconds(60))) {
            refreshAccessToken();
        }
        return credentials.getAccessToken();
    }

    /**
     * Refresh the access token using the refresh token.
     * It sends a POST request to the Google OAuth2 token endpoint with the required parameters.
     */
    private void refreshAccessToken() {
        String body = String.format("client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token", credentials.getClientId(), credentials.getClientSecret(), credentials.getRefreshToken());
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(TOKEN_URL)).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int currentRetry = 0;
        HttpResponse<String> response = null;
        while (currentRetry < MAX_RETRIES) {
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                currentRetry++;
            } catch (IOException | InterruptedException e) {
                logger.error("Error sending request to refresh access token: {}", e.getMessage());
            }
            if (currentRetry == MAX_RETRIES) {
                logger.error("Max retries {} reached. Unable to refresh access token.", MAX_RETRIES);
                throw new RuntimeException("Max retries reached. Unable to refresh access token. " + response.body() + " " + response.statusCode());
            }
            if (response != null && response.statusCode() == 200) {
                currentRetry = MAX_RETRIES; // Exit the loop if the request was successful
            }
            if (response != null && (response.statusCode() == 400 || response.statusCode() == 401 || response.statusCode() == 403)) {
                logger.error(ACCESS_DENIED_INVALID_OR_MISSING_CREDENTIALS);
                throw new AuthenticationError(ACCESS_DENIED_INVALID_OR_MISSING_CREDENTIALS + response.body());
            }
        }
        assert response != null;

        Map<String, Object> json;
        try {
            json = objectMapper.readValue(response.body(), Map.class);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON response: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        String accessToken = (String) json.get("access_token");
        Asserts.check(accessToken != null, "Refresh process: Access token is null");
        int expiresInSeconds = (Integer) json.get("expires_in");
        credentials.setAccessToken(accessToken);
        credentials.setAccessTokenExpiry(Instant.now().plusSeconds(expiresInSeconds));
        logger.info("Access token refreshed successfully. New token: {}...", accessToken.substring(0, 4));
        // Save the new access token to the credentials file
        credentialsRetriever.saveCredentials(credentials);
    }
}

