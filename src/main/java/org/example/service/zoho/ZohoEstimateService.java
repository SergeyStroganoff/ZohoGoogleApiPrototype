package org.example.service.zoho;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.zoho.ZohoErrorResponse;
import org.example.entity.zoho.estimate.ZohoEstimateRequest;
import org.example.entity.zoho.estimate.ZohoEstimateResponse;
import org.example.exception.AuthenticationError;
import org.example.exception.ZohoServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ZohoEstimateService extends ZohoServiceAbstract {

    private static final Logger logger = LoggerFactory.getLogger(ZohoEstimateService.class);
    private static final String ESTIMATE_ENDPOINT = "estimates";

    public ZohoEstimateService(String accessToken, HttpClient httpClient, ObjectMapper objectMapper) {
        super(accessToken, httpClient, objectMapper);
    }

    /**
     * Creates a new estimate in Zoho Invoice.
     *
     * @param zohoEstimateRequest The request object containing estimate details.
     * @param organizationId      The ID of the organisation in Zoho Books.
     * @return A ZohoEstimateResponse containing the created estimate or info about the failure.
     * @throws JsonProcessingException If there is an error serializing the request to JSON.
     */
    public ZohoEstimateResponse createEstimate(ZohoEstimateRequest zohoEstimateRequest, String organizationId) throws JsonProcessingException {
        logger.debug("Adding start estimate to contact: {}", zohoEstimateRequest.getCustomerId());
        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(zohoEstimateRequest);
            logger.debug("Serialized estimate to JSON: {}", jsonPayload);
        } catch (JsonProcessingException e) {
            String msg = "Failed to serialize request to JSON: " + e.getMessage();
            logger.error(msg);
            throw e;
        }
        HttpRequest httpRequest = buildPostRequest(jsonPayload, organizationId, ESTIMATE_ENDPOINT);
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (isSuccessfulStatus(response.statusCode())) {
                logger.debug("Estimate successfully added: {}", response.statusCode());
                return objectMapper.readValue(response.body(), ZohoEstimateResponse.class);
            } else {
                ZohoErrorResponse error = parseError(response);
                String msg = getFriendlyErrorMessage(error);
                logger.error("Failed to add estimate. Status: {}, Zoho code: {}, Message: {}", response.statusCode(),
                        error != null ? error.getCode() : null,
                        error != null ? error.getMessage() : null);
                if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED || response.statusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new AuthenticationError("Authentication error: " + response.statusCode());
                }
                if (response.statusCode() == 429) {
                    throw new ZohoServiceException(ZOHO_API_LIMIT_REACHED);
                }
                throw new ZohoServiceException(msg);
            }
        } catch (IOException | InterruptedException e) {
            String msg = "HTTP request error while adding contact: " + e.getMessage();
            logger.error(msg, e);
            throw new ZohoServiceException(msg, e);
        }
    }
}
