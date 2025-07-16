package org.example.service.zoho;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.zoho.ZohoErrorResponse;
import org.example.entity.zoho.contacts.ZohoContactRequest;
import org.example.entity.zoho.contacts.ZohoContactResponse;
import org.example.exception.AuthenticationError;
import org.example.exception.ZohoServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * ZohoService is a service class that interacts with the Zoho Books API.
 * It provides methods to add new contacts and handle errors from the Zoho API.
 */
public class ZohoContactService extends ZohoServiceAbstract {
    private static final Logger logger = LoggerFactory.getLogger(ZohoContactService.class);
    public static final String CONTACT_ALREADY_EXISTS = "Contact already exists.";

    public static final String ZOHO_OAUTHTOKEN_HEADER = "Zoho-oauthtoken ";
    private static final String CONTACTS_ENDPOINT = "contacts";

    public ZohoContactService(String accessToken, HttpClient httpClient, ObjectMapper objectMapper) {
        super(accessToken, httpClient, objectMapper);
    }

    /**
     * Adds a new contact to Zoho Invoice.
     *
     * @param contactRequest The request object containing contact details.
     * @param organizationId The ID of the organisation in Zoho Books.
     * @return A ZohoContactResponse containing the saved contact or info about the failure.
     * @throws ZohoServiceException If there is an error during the HTTP request.
     */
    public ZohoContactResponse addNewContact(ZohoContactRequest contactRequest, String organizationId) throws JsonProcessingException {
        logger.info("Adding new contact to Zoho: {}", contactRequest.getContactName());
        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(contactRequest);
            logger.debug("Serialized ZohoContactRequest to JSON: {}", jsonPayload);
        } catch (JsonProcessingException e) {
            String msg = "Failed to serialize ZohoContactRequest to JSON: " + e.getMessage();
            logger.error(msg);
            throw e;
        }
        HttpRequest request = buildPostRequest(jsonPayload, organizationId, CONTACTS_ENDPOINT);
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (isSuccessfulStatus(response.statusCode())) {
                logger.debug("Contact successfully added: {}", response.statusCode());
                return objectMapper.readValue(response.body(), ZohoContactResponse.class);
            } else {
                ZohoErrorResponse error = parseError(response);
                String msg = getFriendlyErrorMessage(error);
                logger.error("Failed to add contact. Status: {}, Zoho code: {}, Message: {}", response.statusCode(),
                        error != null ? error.getCode() : null,
                        error != null ? error.getMessage() : null);
                if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED || response.statusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new AuthenticationError("Authentication error: " + response.statusCode());
                }
                if (response.statusCode() == 429) {
                    throw new ZohoServiceException(ZOHO_API_LIMIT_REACHED);
                }
                if (error != null && error.getCode() == 3062) {
                    ZohoContactResponse existingContactResponse = new ZohoContactResponse();
                    existingContactResponse.setCode(3062);
                    existingContactResponse.setMessage(CONTACT_ALREADY_EXISTS);
                    return existingContactResponse;
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
