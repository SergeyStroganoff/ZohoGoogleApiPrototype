package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.google.DistanceGoogleMatrix;
import org.example.entity.google.GoogleMatrixStatus;
import org.example.utils.CredentialsFileRetrieverImpl;
import org.example.utils.CredentialsRetriever;
import org.example.utils.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;

public class GoogleRouteServiceIntegrationTest {
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
    void getRouteEstimate_ReturnRealEstimateResponse() throws Exception {
        // given
        CredentialsRetriever credentialsRetriever = new CredentialsFileRetrieverImpl();
        String apiKey = credentialsRetriever.readCredentials().getMapCredentials().getApiKey();
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;
        GoogleRouteService googleRouteService = new GoogleRouteService(apiKey, httpClient, objectMapper);
        String expectedKilometers = "2,229 km";
        int expectedKilometersValue = 2229357; // 2,229 km in meters
        String expectedTime = "20 hours 9 mins"; // Expected time for the route
        // when
        String origin = "1400 S Lake Shore Dr, Chicago, IL 60605";
        String destination = "1101 Biscayne Blvd, Miami, FL 33132";
        DistanceGoogleMatrix distanceGoogleMatrix = googleRouteService.getRouteEstimate(origin, destination)
                .orElseThrow(() -> new IOException("Failed to get route estimate"));
        // then
        Assertions.assertNotNull(distanceGoogleMatrix, "DistanceGoogleMatrix should not be null");
        Assertions.assertEquals(GoogleMatrixStatus.OK, distanceGoogleMatrix.getStatus(), "Status should be OK");

        String realMiles = distanceGoogleMatrix.getRows()[0].getElements()[0].getDistance().getText();
        int realMilesInValue = distanceGoogleMatrix.getRows()[0].getElements()[0].getDistance().getValue();
        String realTime = distanceGoogleMatrix.getRows()[0].getElements()[0].getDuration().getText();
        Assertions.assertEquals(expectedKilometers, realMiles, "Distance should match expected value");
        Assertions.assertEquals(expectedKilometersValue, realMilesInValue, "Distance should match expected value");
        Assertions.assertEquals(expectedTime, realTime, "Distance should match expected value");
    }
}
