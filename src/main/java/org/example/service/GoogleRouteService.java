package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.example.entity.google.DistanceGoogleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


@Setter
public class GoogleRouteService implements RouteService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleRouteService.class);
    private static final String GOOGLE_MAP_API_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private final String apiKey;
    private HttpClient httpClient;
    private ObjectMapper objectMapper;

    public GoogleRouteService(String apiKey, HttpClient httpClient, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<DistanceGoogleMatrix> getRouteEstimate(String departAddress, String destinationAddress) throws IOException, InterruptedException {
        // Validate input parameters
        if (departAddress == null || departAddress.isBlank()) {
            logger.error("Departure address is null or empty");
            return Optional.empty();
        }
        if (destinationAddress == null || destinationAddress.isBlank()) {
            logger.error("Destination address is null or empty");
            return Optional.empty();
        }
        // Implementation for fetching route estimates from Google Maps API
        String url = String.format("%s?origins=%s&destinations=%s&key=%s",
                GOOGLE_MAP_API_URL,
                encode(departAddress),
                encode(destinationAddress),
                apiKey);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            logger.info("Successfully received response from google map");
            logger.debug("Response body: {}", response.body());
        } else {
            logger.error("Failed to request data. Status: {} Body: {}", response.statusCode(), response.body());
            return Optional.empty();
        }
        return parseResponse(response.body());
    }

    private Optional<DistanceGoogleMatrix> parseResponse(String responseBody) {
        try {
            DistanceGoogleMatrix googleMatrix = objectMapper.readValue(responseBody, DistanceGoogleMatrix.class);
            if (googleMatrix == null) {
                logger.error("Parsed response is null. Response body: {}", responseBody);
                return Optional.empty();
            }
            return Optional.of(googleMatrix);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing response from Google Maps API: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

