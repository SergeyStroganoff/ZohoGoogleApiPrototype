package org.example.service.zoho;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.zoho.ZohoErrorResponse;
import org.example.entity.zoho.contacts.ZohoContactRequest;
import org.example.entity.zoho.contacts.ZohoContactResponse;
import org.example.exception.AuthenticationError;
import org.example.exception.ZohoServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZohoContactServiceTest {
    public static final String EMPTY_JSON = "{}";
    public static final String CODE_9999_MESSAGE_SOMETHING_WENT_WRONG = "{ \"code\": 9999, \"message\": \"Something went wrong\" }";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong";
    public static final String CODE_1005_MESSAGE_CONTACT_ALREADY_EXISTS = "{ \"code\": 1005, \"message\": \"Already exists\" }";
    public static final String MESSAGE_ALREADY_EXISTS = "Already exists";
    public static final String CODE_14_MESSAGE_INVALID_TOKEN = "{ \"code\": 14, \"message\": \"Invalid token\" }";
    public static final String INVALID_TOKEN_MESSAGE = "Invalid token";
    @Mock
    private HttpClient httpClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    HttpResponse<String> httpResponse;
    @InjectMocks
    private ZohoContactService contactService;
    private final String accessToken = "test_token";
    private final String organisationId = "test_org_id";

    @BeforeEach
    void setup() {
        contactService = new ZohoContactService(accessToken, httpClient, objectMapper);
    }

    @Test
    void testAddNewContactSuccess() throws Exception {
        //given
        ZohoContactRequest request = new ZohoContactRequest();
        ZohoContactResponse expectedResponse = new ZohoContactResponse();

        // when
        when(objectMapper.writeValueAsString(request)).thenReturn(EMPTY_JSON);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{ \"code\": 0 }");
        when(objectMapper.readValue(anyString(), eq(ZohoContactResponse.class))).thenReturn(expectedResponse);
        ZohoContactResponse result = contactService.addNewContact(request, organisationId);
        // then
        assertEquals(expectedResponse, result);
        // verify
        verify(httpClient, times(1)).send(any(), any());
        verifyNoMoreInteractions(httpClient);
    }

    /**
     * Test for adding a new contact with a JsonProcessingException.
     * This simulates the scenario where the request object cannot be serialized to JSON.
     */
    @Test
    void testAddNewContactJsonProcessingException() throws Exception {
        // Given
        ZohoContactRequest request = new ZohoContactRequest();
        String organisationId = "some-org-id";
        // Mock the behavior separately before assertThrows
        // When
        when(objectMapper.writeValueAsString(request))
                .thenThrow(JsonProcessingException.class);
        // Then
        assertThrows(JsonProcessingException.class, () ->
                contactService.addNewContact(request, organisationId)
        );
        // verify
        verify(httpClient, times(0)).send(any(), any());
        verifyNoInteractions(httpClient);
    }


    @Test
    void testAddNewContact_AuthenticationError() throws Exception {
        // Given
        ZohoContactRequest request = new ZohoContactRequest();
        // When
        when(objectMapper.writeValueAsString(request)).thenReturn(EMPTY_JSON);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(401);
        when(httpResponse.body()).thenReturn(CODE_14_MESSAGE_INVALID_TOKEN);
        when(objectMapper.readValue(anyString(), eq(ZohoErrorResponse.class)))
                .thenReturn(new ZohoErrorResponse(14, INVALID_TOKEN_MESSAGE));
        // Then
        assertThrows(AuthenticationError.class, () -> contactService.addNewContact(request, organisationId));
    }

    /**
     * Test for adding a new contact with a ZohoErrorResponse indicating that the contact already exists.
     * This simulates the scenario where the Zoho API returns an error response indicating that the contact already exists.
     */

    @Test
    void testAddNewContact_ContactAlreadyExists() throws Exception {
        // Given
        ZohoContactRequest request = new ZohoContactRequest();
        // When
        when(objectMapper.writeValueAsString(request)).thenReturn(EMPTY_JSON);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn(CODE_1005_MESSAGE_CONTACT_ALREADY_EXISTS);
        when(objectMapper.readValue(anyString(), eq(ZohoErrorResponse.class)))
                .thenReturn(new ZohoErrorResponse(1005, MESSAGE_ALREADY_EXISTS));
        ZohoContactResponse result = contactService.addNewContact(request, organisationId);
        // Then
        assertEquals(1005, result.getCode());
        assertEquals("Contact already exists.", result.getMessage());
    }

    /**
     * Test for adding a new contact with a ZohoServiceException.
     * This simulates the scenario where the Zoho API returns generic error response.
     */

    @Test
    void testAddNewContact_ZohoServiceException() throws Exception {
        // Given
        ZohoContactRequest request = new ZohoContactRequest();
        // When
        when(objectMapper.writeValueAsString(request)).thenReturn(EMPTY_JSON);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpResponse.body()).thenReturn(CODE_9999_MESSAGE_SOMETHING_WENT_WRONG);
        when(objectMapper.readValue(anyString(), eq(ZohoErrorResponse.class)))
                .thenReturn(new ZohoErrorResponse(9999, SOMETHING_WENT_WRONG));
        // Then
        assertThrows(ZohoServiceException.class, () -> contactService.addNewContact(request, organisationId));
    }
}
//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme