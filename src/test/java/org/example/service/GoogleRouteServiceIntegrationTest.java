package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.TokenManager;
import org.example.entity.google.DistanceGoogleMatrix;
import org.example.entity.google.GoogleMatrixStatus;
import org.example.service.google.GoogleRouteService;
import org.example.utils.CredentialsFileRetrieverImpl;
import org.example.utils.CredentialsRetriever;
import org.example.utils.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;

class GoogleRouteServiceIntegrationTest {
    /**
     * Tests the Google Maps API for route estimates.
     * This test is intended to be run against a real Google Maps API endpoint.
     * It should be configured with valid credentials and the Google Maps API key.
     * The test will check if the service can handle non-200 responses correctly.
     * Note: Actual implementation of this test would require a live API key and network access.
     *
     * @throws Exception
     */
    @Test
    @Tag("integration")
    void getRouteEstimate_ReturnRealEstimateResponse() throws Exception {
        // given
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;
        OAuthTokenRefresher oAuthTokenRefresher = new OAuthTokenRefresher(httpClient, objectMapper);
        CredentialsRetriever credentialsRetriever = new CredentialsFileRetrieverImpl();
        TokenManager tokenManager = new TokenManager(credentialsRetriever, oAuthTokenRefresher);
        String apiKey = tokenManager.getGoogleMapAPIKey();

        GoogleRouteService googleRouteService = new GoogleRouteService(apiKey, httpClient, objectMapper);
        int expectedKilometersValue = 2217982; // 2,229 km in meters
        int metersTolerance = 10000; // Tolerance in meters for distance comparison
        int secondsTolerance = 600; // Tolerance in seconds for time comparison
        int expectedTimeInSeconds = 72500; // 20 hours 9 minutes in seconds
        // when
        String origin = "1400 S Lake Shore Dr, Chicago, IL 60605";
        String destination = "1101 Biscayne Blvd, Miami, FL 33132";
        DistanceGoogleMatrix distanceGoogleMatrix = googleRouteService.getRouteEstimate(origin, destination)
                .orElseThrow(() -> new IOException("Failed to get route estimate"));
        // then
        Assertions.assertNotNull(distanceGoogleMatrix, "DistanceGoogleMatrix should not be null");
        Assertions.assertEquals(GoogleMatrixStatus.OK, distanceGoogleMatrix.getStatus(), "Status should be OK");

        String realMilesText = distanceGoogleMatrix.getRows()[0].getElements()[0].getDistance().getText();
        int realMilesInValue = distanceGoogleMatrix.getRows()[0].getElements()[0].getDistance().getValue();
        String realTimeText = distanceGoogleMatrix.getRows()[0].getElements()[0].getDuration().getText();
        int realTimeInSeconds = distanceGoogleMatrix.getRows()[0].getElements()[0].getDuration().getValue();

        Assertions.assertTrue(Math.abs(realMilesInValue - expectedKilometersValue) <= metersTolerance,
                String.format("Distance in meters should be within %d of expected value %d. Actual: %d",
                        metersTolerance, expectedKilometersValue, realMilesInValue));
        Assertions.assertTrue(Math.abs(realTimeInSeconds - expectedTimeInSeconds) <= secondsTolerance,
                String.format("Time in seconds should be within %d of expected value %d. Actual: %d",
                        secondsTolerance, expectedTimeInSeconds, realTimeInSeconds));
        Assertions.assertFalse(realMilesText.isEmpty(), "Real miles text should not be empty");
        Assertions.assertFalse(realTimeText.isEmpty(), "Real time text should not be empty");
    }
}
