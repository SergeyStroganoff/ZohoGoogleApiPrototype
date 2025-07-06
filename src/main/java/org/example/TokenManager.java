package org.example;

import org.example.entity.AccessToken;
import org.example.entity.AppCredentials;
import org.example.entity.EndPoint;
import org.example.exception.CredentialsRetrieverException;
import org.example.service.OAuthTokenRefresher;
import org.example.utils.CredentialsRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;

/**
 * GoogleTokenManager is a class that manages the OAuth2 access token for Google APIs.
 * It handles the refresh token flow and provides a method to get the current access token.
 */
public class TokenManager {
    public static final String ERROR_RETRIEVING_CREDENTIALS = "Error retrieving credentials";
    public static final String CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE = "Fatal error - credentials are not initialized.";
    /**
     * The AppCredentials object contains the client ID, client secret, access token, and refresh token.
     */
    private AppCredentials credentials;
    private final CredentialsRetriever credentialsRetriever;
    private final OAuthTokenRefresher oAuthTokenRefresher;
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);


    public TokenManager(CredentialsRetriever credentialsRetriever, OAuthTokenRefresher oAuthTokenRefresher) {
        this.credentialsRetriever = credentialsRetriever;
        this.oAuthTokenRefresher = oAuthTokenRefresher;
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
    public synchronized String getGoogleCalendarAccessToken() throws IOException, InterruptedException {
        Instant now = Instant.now();
        if (now.isAfter(credentials.getCalendarCredentials().getAccessTokenExpiry().minusSeconds(60))) {
            AccessToken accessToken = oAuthTokenRefresher.refreshOAuthAccessToken(
                    credentials.getCalendarCredentials().getClientId(),
                    credentials.getCalendarCredentials().getClientSecret(),
                    credentials.getCalendarCredentials().getRefreshToken(),
                    EndPoint.GOOGLE_TOKEN_REFRESH.getUrl()
            );
            credentials.getCalendarCredentials().setAccessToken(accessToken.accessToken());
            credentials.getCalendarCredentials().setAccessTokenExpiry(accessToken.expiresAt());
            credentialsRetriever.updateCredentials(credentials);
        }
        return credentials.getCalendarCredentials().getAccessToken();
    }


    /**
     * Get the Zoho Invoice access token. If the current access token is expired or about to expire,
     * it refreshes the token using the refresh token.
     *
     * @return The Zoho Invoice access token.
     * @throws CredentialsRetrieverException If there is an error retrieving the credentials.
     */
    public String getZOHOInvoiceAccessToken() throws IOException, InterruptedException {
        Instant now = Instant.now();
        if (now.isAfter(credentials.getZohoCredentials().getAccessTokenExpiry().minusSeconds(60))) {
            AccessToken accessToken = oAuthTokenRefresher.refreshOAuthAccessToken(
                    credentials.getZohoCredentials().getClientId(),
                    credentials.getZohoCredentials().getClientSecret(),
                    credentials.getZohoCredentials().getRefreshToken(),
                    EndPoint.ZOHO_TOKEN_REFRESH.getUrl()
            );
            credentials.getZohoCredentials().setAccessToken(accessToken.accessToken());
            credentials.getZohoCredentials().setAccessTokenExpiry(accessToken.expiresAt());
            credentialsRetriever.updateCredentials(credentials);
        }
        return credentials.getZohoCredentials().getAccessToken();
    }

    /**
     * Get the Zoho Invoice organisation ID from the credentials.
     *
     * @return The Zoho Invoice organisation ID.
     * @throws CredentialsRetrieverException If there is an error retrieving the credentials.
     */
    public String getZOHOInvoiceOrganisationId() {
        if (credentials.getZohoCredentials() != null) {
            logger.debug("Retrieving Zoho Invoice organisation ID {}", credentials.getZohoCredentials().getOrganisationId());
            return credentials.getZohoCredentials().getOrganisationId();
        } else {
            logger.error(CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE);
            throw new CredentialsRetrieverException(CREDENTIALS_ARE_NOT_INITIALIZED_MESSAGE);
        }
    }
}

