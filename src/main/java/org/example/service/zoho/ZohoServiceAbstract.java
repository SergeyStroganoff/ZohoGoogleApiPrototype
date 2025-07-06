package org.example.service.zoho;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.zoho.ZohoErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.example.service.zoho.ZohoContactService.*;

public abstract class ZohoServiceAbstract {
    protected static final Logger logger = LoggerFactory.getLogger(ZohoServiceAbstract.class);
    private static final String ZOHO_INVOICE_API_URL = "https://www.zohoapis.com/invoice/v3/";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ORG_ID = "X-com-zoho-invoice-organizationid";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    protected final String accessToken;
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected ZohoServiceAbstract(String accessToken, HttpClient httpClient, ObjectMapper objectMapper) {
        this.accessToken = accessToken;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    protected boolean isSuccessfulStatus(int statusCode) {
        return statusCode >= HttpURLConnection.HTTP_OK && statusCode < HttpURLConnection.HTTP_MULT_CHOICE;
    }

    /**
     * Parses the error response from Zoho API.
     *
     * @param response The HttpResponse containing the error body.
     * @return A ZohoErrorResponse object containing the error details.
     */
    protected ZohoErrorResponse parseError(HttpResponse<String> response) {
        try {
            return objectMapper.readValue(response.body(), ZohoErrorResponse.class);
        } catch (Exception e) {
            logger.warn("Failed to parse Zoho error response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Returns a user-friendly error message based on the Zoho error code.
     *
     * @param error The ZohoErrorResponse containing the error code and message.
     * @return A user-friendly error message.
     */
    protected String getFriendlyErrorMessage(ZohoErrorResponse error) {
        if (error == null) return "Unknown error (could not parse response)";
        return switch (error.getCode()) {
            case 14 -> "Invalid or expired access token.";
            case 1001 -> "Mandatory parameter is missing. Check the contact data.";
            case 1002 -> "Invalid parameter value.";
            case 1005, 3062 -> CONTACT_ALREADY_EXISTS;
            case 1038 -> ZOHO_API_LIMIT_REACHED;
            default -> "Zoho error code " + error.getCode() + ": " + error.getMessage();
        };
    }

    protected HttpRequest buildPostRequest(String jsonPayload, String organisationId, String endPoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(ZOHO_INVOICE_API_URL + endPoint))
                .header(HEADER_AUTHORIZATION, ZOHO_OAUTHTOKEN_HEADER + accessToken)
                .header(HEADER_ORG_ID, organisationId)
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
    }
}
