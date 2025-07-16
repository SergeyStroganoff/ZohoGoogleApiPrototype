package org.example.service.zoho;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.zoho.estimate.LineItem;
import org.example.entity.zoho.estimate.ZohoEstimateRequest;
import org.example.entity.zoho.estimate.ZohoEstimateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZohoEstimateServiceTest {
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<String> httpResponse;
    private ZohoEstimateService zohoEstimateService;
    private static final String ACCESS_TOKEN = "dummyAccessToken";
    private static final String ORGANIZATION_ID = "123456789";
    private static final String CUSTOMER_ID = "5971371000000000012";

    @BeforeEach
    void setUp() {
        zohoEstimateService = new ZohoEstimateService(ACCESS_TOKEN, httpClient, new ObjectMapper());
    }

    @Test
    void createEstimate_shouldReturnSuccess_whenValidRequest() throws Exception {
        // Arrange: Build a simple estimate request with diagnostic service
        ZohoEstimateRequest request = new ZohoEstimateRequest();
        request.setCustomerId(CUSTOMER_ID);
        request.setLineItems(List.of(
                new LineItem("1", "Diagnostic visit", 150.0, 1)
        ));

        // Prepare HTTP response
        String jsonResponse = """
                {
                  "code": 0,
                  "message": "Estimate has been created.",
                  "estimate": {
                    "estimate_id": "987654321",
                    "customer_id": "%s",
                    "line_items": [
                      {
                        "name": "Diagnostic visit",
                        "rate": 70.00,
                        "quantity": 1
                      }
                    ],
                    "status": "draft"
                  }
                }
                """.formatted(CUSTOMER_ID);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(jsonResponse);
        when(httpResponse.statusCode()).thenReturn(201);
        // Act
        ZohoEstimateResponse response = zohoEstimateService.createEstimate(request, ORGANIZATION_ID);
        // Assert
        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertEquals("Estimate has been created.", response.getMessage());
        assertEquals("draft", response.getEstimate().getStatus());
        assertEquals(1, response.getEstimate().getLineItems().size());
        assertEquals("Diagnostic visit", response.getEstimate().getLineItems().get(0).getName());
        assertEquals(70.00, response.getEstimate().getLineItems().get(0).getRate());
    }
}
