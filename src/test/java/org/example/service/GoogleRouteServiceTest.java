package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.MockHttpResponse;
import org.example.entity.google.DistanceGoogleMatrix;
import org.example.entity.google.GoogleMatrixStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleRouteServiceTest {

    @Mock
    HttpClient httpClient;
    ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    GoogleRouteService googleRouteService;

    @BeforeEach
    void setUp() {
        googleRouteService.setObjectMapper(objectMapper);
    }

    @Test
    void getRouteEstimate_ReturnNotSuccessResponse_WhenStatusNot200() throws Exception {
        // Given
        String jsonResponse = """
                {
                     "error_message": "Invalid request",
                     "status": "INVALID_REQUEST"
                 }
                """;
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();

        // When
        when(httpClient.send(any(HttpRequest.class), eq(bodyHandler))).thenReturn(new MockHttpResponse(401, jsonResponse));
        Optional<DistanceGoogleMatrix> result = googleRouteService.getRouteEstimate("New York, NY, USA", "Washington, DC, USA");
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getRouteEstimate_ReturnExpectedRouteEstimate() throws Exception {
        // Given
        String jsonResponse = """
                {
                     "destination_addresses": [
                         "New York, NY, USA"
                     ],
                     "origin_addresses": [
                         "Washington, DC, USA"
                     ],
                     "rows": [
                         {
                             "elements": [
                                 {
                                     "distance": {
                                         "text": "228 mi",
                                         "value": 367309
                                     },
                                     "duration": {
                                         "text": "3 hours 49 mins",
                                         "value": 13756
                                     },
                                     "status": "OK"
                                 }
                             ]
                         }
                     ],
                     "status": "OK"
                 }
                """;
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
        // When
        when(httpClient.send(any(HttpRequest.class), eq(bodyHandler))).thenReturn(new MockHttpResponse(200, jsonResponse));
        Optional<DistanceGoogleMatrix> result = googleRouteService.getRouteEstimate("New York, NY, USA", "Washington, DC, USA");
        // Then
        assertTrue(result.isPresent());
        assertEquals(GoogleMatrixStatus.OK, result.get().getStatus());
        assertEquals("228 mi", result.get().getRows()[0].getElements()[0].getDistance().getText());
        assertEquals("3 hours 49 mins", result.get().getRows()[0].getElements()[0].getDuration().getText());
    }
}

