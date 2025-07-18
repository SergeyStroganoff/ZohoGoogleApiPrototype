package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DynamoDbEventDeduplicationService.
 * Tests cover all scenarios including validation, idempotency, error handling, and TTL calculation.
 */
@ExtendWith(MockitoExtension.class)
class DynamoDbEventDeduplicationServiceTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    private DynamoDbEventDeduplicationService service;
    
    private static final String VALID_EVENT_ID = "rn1nbm9c1u5lutjt1smo062f7k";
    private static final String TABLE_NAME = "event-deduplication";
    private static final long TTL_DAYS = 30;

    @BeforeEach
    void setUp() {
        service = new DynamoDbEventDeduplicationService(dynamoDbClient, TABLE_NAME, TTL_DAYS);
    }

    @Test
    void testIsEventProcessed_EventNotExists_ReturnsFalse() {
        // Given
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().message("Item not found").build());

        // When
        boolean result = service.isEventProcessed(VALID_EVENT_ID);

        // Then
        assertFalse(result);
        verify(dynamoDbClient, times(1)).getItem(any(GetItemRequest.class));
    }

    @Test
    void testIsEventProcessed_EventExists_ReturnsTrue() {
        // Given
        GetItemResponse response = GetItemResponse.builder()
                .item(Map.of(
                        "event_id", AttributeValue.builder().s(VALID_EVENT_ID).build(),
                        "expires_at", AttributeValue.builder().n("1752362040").build()
                ))
                .build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

        // When
        boolean result = service.isEventProcessed(VALID_EVENT_ID);

        // Then
        assertTrue(result);
        verify(dynamoDbClient, times(1)).getItem(any(GetItemRequest.class));
    }

    @Test
    void testMarkEventProcessed_NewEvent_Success() {
        // Given
        PutItemResponse response = PutItemResponse.builder().build();
        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(response);

        assertDoesNotThrow(() -> service.markEventProcessed(VALID_EVENT_ID));
        verify(dynamoDbClient, times(1)).putItem(any(PutItemRequest.class));
    }

    @Test
    void testMarkEventProcessed_IdempotentBehavior_NoException() {
        // Given
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().message("Item already exists").build());

        assertDoesNotThrow(() -> service.markEventProcessed(VALID_EVENT_ID));
        verify(dynamoDbClient, times(1)).putItem(any(PutItemRequest.class));
    }

    @Test
    void testMarkEventProcessed_DynamoDbException_ThrowsRuntimeException() {
        // Given
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(DynamoDbException.builder().message("Service unavailable").build());

        assertThrows(RuntimeException.class, () -> service.markEventProcessed(VALID_EVENT_ID));
        verify(dynamoDbClient, times(1)).putItem(any(PutItemRequest.class));
    }

    @Test
    void testValidation_NullEventId_ThrowsIllegalArgumentException() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.isEventProcessed(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.markEventProcessed(null))
        );
        verifyNoInteractions(dynamoDbClient);
    }

    @Test
    void testValidation_EmptyEventId_ThrowsIllegalArgumentException() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.isEventProcessed("")),
                () -> assertThrows(IllegalArgumentException.class, () -> service.markEventProcessed(""))
        );
        verifyNoInteractions(dynamoDbClient);
    }

    @Test
    void testValidation_BlankEventId_ThrowsIllegalArgumentException() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.isEventProcessed("   ")),
                () -> assertThrows(IllegalArgumentException.class, () -> service.markEventProcessed("   "))
        );
        verifyNoInteractions(dynamoDbClient);
    }

    @Test
    void testValidation_TooLongEventId_ThrowsIllegalArgumentException() {
        // Given
        String longEventId = "a".repeat(256);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.isEventProcessed(longEventId)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.markEventProcessed(longEventId))
        );
        verifyNoInteractions(dynamoDbClient);
    }

    @Test
    void testValidation_MaxLengthEventId_Success() {
        // Given
        String maxLengthEventId = "a".repeat(255);
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().message("Item not found").build());

        assertDoesNotThrow(() -> service.isEventProcessed(maxLengthEventId));
        verify(dynamoDbClient, times(1)).getItem(any(GetItemRequest.class));
    }

    @Test
    void testTtlCalculation_CorrectExpirationTime() {
        // Given
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long expectedMinExpiration = currentTimeSeconds + (TTL_DAYS * 24 * 60 * 60);
        long expectedMaxExpiration = expectedMinExpiration + 5; // Allow 5 seconds tolerance

        // When
        service.markEventProcessed(VALID_EVENT_ID);

        // Then
        verify(dynamoDbClient, times(1)).putItem(argThat(request -> {
            AttributeValue expiresAtValue = request.item().get("expires_at");
            assertNotNull(expiresAtValue);
            long actualExpiration = Long.parseLong(expiresAtValue.n());
            return actualExpiration >= expectedMinExpiration && actualExpiration <= expectedMaxExpiration;
        }));
    }

    @Test
    void testConstructor_DefaultTtl() {
        // When
        DynamoDbEventDeduplicationService defaultService = new DynamoDbEventDeduplicationService(dynamoDbClient, TABLE_NAME);

        // Then
        assertNotNull(defaultService);
    }

    @Test
    void testConstructor_CustomTtl() {
        // Given
        long customTtl = 7;

        // When
        DynamoDbEventDeduplicationService customService = new DynamoDbEventDeduplicationService(dynamoDbClient, TABLE_NAME, customTtl);

        // Then
        assertNotNull(customService);
    }
}
