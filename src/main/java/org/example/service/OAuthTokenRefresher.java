package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.AccessToken;
import org.example.exception.AuthenticationError;
import org.example.exception.TokenRefreshException;
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
 * OAuthTokenRefresher is a service class that handles the refreshing of OAuth access tokens.
 * It uses an HTTP client to send requests to the token refresh endpoint and parses the response
 * to extract the new access token and its expiration time.
 */
public class OAuthTokenRefresher {
    public static final String UNABLE_TO_REFRESH_ACCESS_TOKEN = "Max retries {} reached. Unable to refresh access token.";
    public static final String EXPIRES_IN_PROP = "expires_in";
    public static final String MISSING_PROPERTY_IN_RESPONSE = "Missing " + EXPIRES_IN_PROP + " property in response";
    public static final String ERROR_PARSING_JSON_RESPONSE = "Error parsing JSON response";
    public static final String WAS_INTERRUPTED = "Retry sleep delay was interrupted";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRIES = 3;
    private static final Logger logger = LoggerFactory.getLogger(OAuthTokenRefresher.class);
    public static final String ACCESS_DENIED_INVALID_OR_MISSING_CREDENTIALS = "Access denied: Invalid or missing credentials";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ACCESS_TOKEN_IS_NULL_MESSAGE = "Access token is null";
    public static final String REFRESHED_SUCCESSFULLY = "Access token refreshed successfully.";

    public OAuthTokenRefresher(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Refreshes the OAuth access token using the provided client ID, client secret, refresh token, and token refresh endpoint.
     *
     * @param clientId             The client ID for the OAuth application.
     * @param clientSecret         The client secret for the OAuth application.
     * @param refreshToken         The refresh token to use for refreshing the access token.
     * @param tokenRefreshEndpoint The endpoint to send the refresh request to.
     * @return An AccessToken object containing the new access token and its expiration time.
     * @throws AuthenticationError  If the refresh fails due to invalid credentials or other authentication issues.
     * @throws InterruptedException If the thread is interrupted while waiting.
     * @throws IOException          If an I/O error occurs during the request.
     */
    public AccessToken refreshOAuthAccessToken(String clientId, String clientSecret, String refreshToken, String tokenRefreshEndpoint) throws AuthenticationError, InterruptedException, IOException {
        String body = String.format("client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                clientId,
                clientSecret,
                refreshToken);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(tokenRefreshEndpoint)).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        int currentRetry = 0;
        HttpResponse<String> response = null;
        while (currentRetry < MAX_RETRIES) {
            currentRetry++;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (isResponseSuccess(response)) {
                    break;
                } else if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED || response.statusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    logger.error("Token refresh failed. Status: {}, Response: {}", response.statusCode(), response);
                    throw new AuthenticationError(ACCESS_DENIED_INVALID_OR_MISSING_CREDENTIALS + response.statusCode());
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Error sending request to refresh access token: {}", e.getMessage());
                if (e instanceof InterruptedException) {
                    throw e;
                }
            }
            try {
                long delayMillis = Math.min(1000, 100 * (1L << (currentRetry - 1))); // 100ms, 200ms, 400ms, ...
                Thread.sleep(delayMillis);
            } catch (InterruptedException ie) {
                logger.warn(WAS_INTERRUPTED);
                throw ie;
            }
        }
        if (isResponseSuccess(response)) {
            return parseAccessToken(response);
        } else {
            logger.error(UNABLE_TO_REFRESH_ACCESS_TOKEN, MAX_RETRIES);
            throw new TokenRefreshException(UNABLE_TO_REFRESH_ACCESS_TOKEN + " " + (response != null ? response.body() : "No response received"));
        }
    }

    private AccessToken parseAccessToken(HttpResponse<String> response) {
        Map<String, Object> json;
        try {
            json = objectMapper.readValue(response.body(), Map.class);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_PARSING_JSON_RESPONSE, e);
            throw new TokenRefreshException(ERROR_PARSING_JSON_RESPONSE, e);
        }
        String accessToken = (String) json.get(ACCESS_TOKEN);
        if (accessToken == null) {
            logger.error(ACCESS_TOKEN_IS_NULL_MESSAGE);
            throw new TokenRefreshException(ACCESS_TOKEN_IS_NULL_MESSAGE);
        }
        Number expiresInSeconds = (Number) json.get(EXPIRES_IN_PROP);
        if (expiresInSeconds == null) {
            throw new TokenRefreshException(MISSING_PROPERTY_IN_RESPONSE);
        }
        logger.info(REFRESHED_SUCCESSFULLY);
        return new AccessToken(accessToken, Instant.now().plusSeconds(expiresInSeconds.longValue()));
    }

    private boolean isResponseSuccess(HttpResponse<String> response) {
        return response != null && response.statusCode() >= HttpURLConnection.HTTP_OK && response.statusCode() < HttpURLConnection.HTTP_MULT_CHOICE;
    }
}
