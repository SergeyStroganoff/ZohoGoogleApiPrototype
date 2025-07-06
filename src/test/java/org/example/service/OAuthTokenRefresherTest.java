package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.AccessToken;
import org.example.exception.AuthenticationError;
import org.example.exception.TokenRefreshException;
import org.example.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthTokenRefresherTest {
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String ENDPOINT = "https://example.com/token";
    @Mock
    HttpClient httpClient;
    @Mock
    private HttpResponse<String> httpResponse;

    private final ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;

    private OAuthTokenRefresher refresher;

    @BeforeEach
    void setup() {
        refresher = new OAuthTokenRefresher(httpClient, objectMapper);
    }

    @Test
    void testSuccessfulTokenRefresh() throws Exception {
        //given
        String responseBody = "{\"access_token\":\"test-token\",\"expires_in\":3600}";
        //when
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        AccessToken token = refresher.refreshOAuthAccessToken(
                CLIENT_ID, CLIENT_SECRET, REFRESH_TOKEN, ENDPOINT
        );
        //then
        assertNotNull(token);
        assertEquals("test-token", token.accessToken());
        assertTrue(token.expiresAt().isAfter(Instant.now()));
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testAuthErrorThrowsAuthenticationError() throws Exception {
        //when
        when(httpResponse.statusCode()).thenReturn(401);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        //then
        assertThrows(AuthenticationError.class, () ->
                refresher.refreshOAuthAccessToken(
                        CLIENT_ID, CLIENT_SECRET, REFRESH_TOKEN, ENDPOINT
                ));
    }

    @Test
    void testJsonParseExceptionThrowsTokenRefreshException() throws Exception {
        //given
        String invalidJson = "not a json";
        //when
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(invalidJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        //then
        assertThrows(TokenRefreshException.class, () ->
                refresher.refreshOAuthAccessToken(
                        CLIENT_ID, CLIENT_SECRET, REFRESH_TOKEN, ENDPOINT
                ));
    }

    @Test
    void testRetryExceededThrowsTokenRefreshException() throws Exception {
        //when
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("network error"));

        assertThrows(TokenRefreshException.class, () ->
                refresher.refreshOAuthAccessToken(
                        CLIENT_ID, CLIENT_SECRET, REFRESH_TOKEN, ENDPOINT
                ));
        //then
        verify(httpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testInterruptedExceptionPropagates() throws Exception {
        //when
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("interrupted"));
        //then
        assertThrows(InterruptedException.class, () ->
                refresher.refreshOAuthAccessToken(
                        CLIENT_ID, CLIENT_SECRET, REFRESH_TOKEN, ENDPOINT
                ));
    }
}
