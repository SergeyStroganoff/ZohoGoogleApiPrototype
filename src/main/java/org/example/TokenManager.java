package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.AppCredentials;
import org.example.exception.AuthenticationError;
import org.example.exception.CredentialsRetrieverException;
import org.example.utils.CredentialsRetriever;
import org.example.utils.JsonUtils;
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
public class TokenManager {
    public static final String ACCESS_DENIED_INVALID_OR_MISSING_CREDENTIALS = "Access denied: Invalid or missing credentials";
    public static final String ERROR_RETRIEVING_CREDENTIALS = "Error retrieving credentials";
    /**
     * The AppCredentials object contains the client ID, client secret, access token, and refresh token.
     */
    private AppCredentials credentials;
    private final CredentialsRetriever credentialsRetriever;
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;
    private static final int MAX_RETRIES = 3;

    public TokenManager(CredentialsRetriever credentialsRetriever) {
        this.credentialsRetriever = credentialsRetriever;
        credentials = credentialsInitialization(credentials);
    }
    /**
     * Get the Google Maps API key from the credentials.
     *
     * @return The Google Maps API key.
     * @throws CredentialsRetrieverException If there is an error retrieving the credentials.
     */
    public String getGoogleMapAPIKey() throws CredentialsRetrieverException {
        return credentials.getMapCredentials().getApiKey();
    }

    /**
     * Get the new access token. If the current access token is expired or about to expire,
     * it refreshes the token using the refresh token.
     */
    public String getGoogleCalendarAccessToken() {
        Instant now = Instant.now();
        if (now.isAfter(credentials.getCalendarCredentials().getAccessTokenExpiry().minusSeconds(60))) {
            refreshGoogleCalendarAccessToken();
        }
        return credentials.getCalendarCredentials().getAccessToken();
    }

    /**
     * Refresh the access token using the refresh token.
     * It sends a POST request to the Google OAuth2 token endpoint with the required parameters.
     */
    private void refreshGoogleCalendarAccessToken() {
        String body = String.format("client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                credentials.getCalendarCredentials().getClientId(),
                credentials.getCalendarCredentials().getClientSecret(),
                credentials.getCalendarCredentials().getRefreshToken());
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
        if (accessToken == null) {
            logger.error("Access token is null");
            throw new RuntimeException("Access token is null");
        }
        int expiresInSeconds = (Integer) json.get("expires_in");
        credentials.getCalendarCredentials().setAccessToken(accessToken);
        credentials.getCalendarCredentials().setAccessTokenExpiry(Instant.now().plusSeconds(expiresInSeconds));
        logger.info("Access token refreshed successfully. New token: {}...", accessToken.substring(0, 4));
        // Save the new access token to the credentials file
        credentialsRetriever.saveCredentials(credentials);
    }

    private AppCredentials credentialsInitialization(AppCredentials credentials) throws CredentialsRetrieverException {
        if (credentials == null) {
            try {
                credentials = credentialsRetriever.readCredentials();
            } catch (IOException e) {
                logger.error(ERROR_RETRIEVING_CREDENTIALS + "  {}", e.getMessage());
                throw new CredentialsRetrieverException(ERROR_RETRIEVING_CREDENTIALS, e);
            }
        }
        return credentials;
    }
}

