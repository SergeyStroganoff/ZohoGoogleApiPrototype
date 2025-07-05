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
import java.net.HttpURLConnection;
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
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ACCESS_TOKEN_IS_NULL_MESSAGE = "Access token is null";
    public static final String EXPIRES_IN_PROP = "expires_in";
    public static final String REFRESHED_SUCCESSFULLY = "Access token refreshed successfully.";
    public static final String CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE = "Fatal error - credentials are not initialized.";
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
        credentials = credentialsLoad();
    }

    /**
     * Load the credentials from the credentials file.
     * If the credentials are not found, it throws a CredentialsRetrieverException.
     *
     * @return The AppCredentials object containing the credentials.
     * @throws CredentialsRetrieverException If there is an error retrieving the credentials.
     */
    private AppCredentials credentialsLoad() throws CredentialsRetrieverException {
        try {
            credentials = credentialsRetriever.readCredentials();
        } catch (IOException e) {
            logger.error(ERROR_RETRIEVING_CREDENTIALS + "  {}", e.getMessage());
            throw new CredentialsRetrieverException(ERROR_RETRIEVING_CREDENTIALS, e);
        }
        return credentials;
    }

    /**
     * Get the Google Maps API key from the credentials.
     *
     * @return The Google Maps API key.
     * @throws CredentialsRetrieverException If there is an error retrieving the credentials.
     */
    public String getGoogleMapAPIKey() throws CredentialsRetrieverException {
        if (credentials.getMapCredentials() != null) {
            return credentials.getMapCredentials().getApiKey();

        } else {
            logger.error(CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE);
            throw new CredentialsRetrieverException(CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE);
        }

    }

    /**
     * Get the new access token. If the current access token is expired or about to expire,
     * it refreshes the token using the refresh token.
     */
    public synchronized String getGoogleCalendarAccessToken() {
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
            if (response != null) {
                if (response.statusCode() >= HttpURLConnection.HTTP_OK && response.statusCode() < HttpURLConnection.HTTP_MULT_CHOICE) {
                    break;
                } else if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED || response.statusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    logger.error(ACCESS_DENIED_INVALID_OR_MISSING_CREDENTIALS);
                    throw new AuthenticationError(ACCESS_DENIED_INVALID_OR_MISSING_CREDENTIALS + response.body());
                }
            }
            if (currentRetry == MAX_RETRIES) {
                logger.error("Max retries {} reached. Unable to refresh access token.", MAX_RETRIES);
                String errorMessage = response != null ? response.body() : "No response received";
                throw new RuntimeException("Max retries reached. Unable to refresh access token. " + errorMessage);
            }
        }
        Map<String, Object> json;
        try {
            json = objectMapper.readValue(response.body(), Map.class);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON response: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        String accessToken = (String) json.get(ACCESS_TOKEN);
        if (accessToken == null) {
            logger.error(ACCESS_TOKEN_IS_NULL_MESSAGE);
            throw new RuntimeException(ACCESS_TOKEN_IS_NULL_MESSAGE);
        }
        Integer expiresInSeconds = (Integer) json.get(EXPIRES_IN_PROP);
        if (expiresInSeconds == null) {
            throw new RuntimeException("Missing 'expires_in' property in response");
        }
        credentials.getCalendarCredentials().setAccessToken(accessToken);
        credentials.getCalendarCredentials().setAccessTokenExpiry(Instant.now().plusSeconds(expiresInSeconds));
        logger.info(REFRESHED_SUCCESSFULLY);
        // Save the new access token to the credentials file
        credentialsRetriever.saveCredentials(credentials);
    }


    /**
     * Get the Zoho Invoice access token. If the current access token is expired or about to expire,
     * it refreshes the token using the refresh token.
     *
     * @return The Zoho Invoice access token.
     * @throws CredentialsRetrieverException If there is an error retrieving the credentials.
     */
    //todo check this method
    public String getZOHOInvoiceAccessToken() {
        if (credentials.getZohoCredentials() != null) {
            Instant now = Instant.now();
            if (now.isAfter(credentials.getZohoCredentials().getAccessTokenExpiry().minusSeconds(60))) {
                refreshZOHOInvoiceAccessToken();
            }
            return credentials.getZohoCredentials().getAccessToken();
        } else {
            logger.error(CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE);
            throw new CredentialsRetrieverException(CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE);
        }
    }

    /**
     * Refresh the Zoho Invoice access token using the refresh token.
     * It sends a POST request to the Zoho OAuth2 token endpoint with the required parameters.
     */
    //todo check this method
    private void refreshZOHOInvoiceAccessToken() {
        String body = String.format("client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                credentials.getZohoCredentials().getClientId(),
                credentials.getZohoCredentials().getClientSecret(),
                credentials.getZohoCredentials().getRefreshToken());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        int currentRetry = 0;
        HttpResponse<String> response = null;
        while (currentRetry < MAX_RETRIES) {
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                currentRetry++;
            } catch (IOException | InterruptedException e) {
                logger.error("Error sending request to refresh Zoho access token: {}", e.getMessage());
            }
            if (currentRetry == MAX_RETRIES) {
                logger.error("Max retries {} reached. Unable to refresh Zoho access token.", MAX_RETRIES);
                throw new RuntimeException("Max retries reached. Unable to refresh Zoho access token. " + response.body() + " " + response.statusCode());
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
        String accessToken = (String) json.get(ACCESS_TOKEN);
        if (accessToken == null) {
            logger.error(ACCESS_TOKEN_IS_NULL_MESSAGE);
            throw new RuntimeException(ACCESS_TOKEN_IS_NULL_MESSAGE);
        }
        int expiresInSeconds = (Integer) json.get(EXPIRES_IN_PROP);
        credentials.getZohoCredentials().setAccessToken(accessToken);
    }

    /**
     * Get the Zoho Invoice organisation ID from the credentials.
     *
     * @return The Zoho Invoice organisation ID.
     * @throws CredentialsRetrieverException If there is an error retrieving the credentials.
     */
    //todo check this method
    public String getZOHOInvoiceOrganisationId() {
        if (credentials.getZohoCredentials() != null) {
            logger.debug("Retrieving Zoho Invoice organisation ID {}", credentials.getZohoCredentials().getOrganisationId());
            //  throw new UnsupportedOperationException("Method getZOHOInvoiceOrganisationId is not implemented yet.");
            return credentials.getZohoCredentials().getOrganisationId();
        } else {
            logger.error(CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE);
            throw new CredentialsRetrieverException(CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE);
        }
    }
}

