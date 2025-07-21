package org.example.service.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;

/**
 * Service for deduplication of Google Calendar events using Amazon DynamoDB.
 * 
 * This service provides methods to check if an event has been processed and to mark
 * events as processed with automatic TTL (Time To Live) for cleanup.
 * 
 * The service uses DynamoDB conditional writes to ensure atomic operations and
 * prevent race conditions when multiple processes try to add the same event.
 */
public class DynamoDbEventDeduplicationService {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbEventDeduplicationService.class);

    private static final String EVENT_ID_ATTRIBUTE = "event_id";
    private static final String EXPIRES_AT_ATTRIBUTE = "expires_at";
    private static final String CONDITION_EXPRESSION = "attribute_not_exists(" + EVENT_ID_ATTRIBUTE + ")";
    private static final int MAX_EVENT_ID_LENGTH = 255;
    private static final int DEFAULT_TTL_DAYS = 30;
    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    private final long ttlDays;

    /**
     * Creates a new DynamoDbEventDeduplicationService with default TTL of 30 days.
     *
     * @param dynamoDbClient The DynamoDB client to use for operations
     * @param tableName The name of the DynamoDB table to use for deduplication
     */
    public DynamoDbEventDeduplicationService(DynamoDbClient dynamoDbClient, String tableName) {
        this(dynamoDbClient, tableName, DEFAULT_TTL_DAYS);
    }
    
    /**
     * Creates a new DynamoDbEventDeduplicationService with custom TTL.
     * 
     * @param dynamoDbClient The DynamoDB client to use for operations
     * @param tableName The name of the DynamoDB table to use for deduplication
     * @param ttlDays The number of days after which entries should expire
     */
    public DynamoDbEventDeduplicationService(DynamoDbClient dynamoDbClient, String tableName, long ttlDays) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
        this.ttlDays = ttlDays;
    }
    
    /**
     * Checks if an event with the given eventId has already been processed.
     * 
     * @param eventId The unique identifier of the event to check
     * @return true if the event has been processed, false otherwise
     * @throws IllegalArgumentException if eventId is null, empty, or longer than 255 characters
     */
    public boolean isEventProcessed(String eventId) {
        validateEventId(eventId);
        try {
            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of(EVENT_ID_ATTRIBUTE, AttributeValue.builder().s(eventId).build()))
                    .build();

            GetItemResponse response = dynamoDbClient.getItem(request);
            boolean exists = response.hasItem() && !response.item().isEmpty();
            logger.debug("Event {} processed check: {}", eventId, exists);
            return exists;

        } catch (ResourceNotFoundException e) {
            logger.debug("Event {} not found in table {}", eventId, tableName);
            return false;
        } catch (DynamoDbException e) {
            logger.error("Failed to check if event {} is processed: {}", eventId, e.getMessage());
            throw e;
        }
    }

    /**
     * Marks an event as processed by adding it to the DynamoDB table.
     * This method is idempotent - calling it multiple times with the same eventId
     * will not cause an error.
     *
     * The method automatically calculates an expires_at field (current time + TTL days)
     * for DynamoDB TTL auto-deletion.
     *
     * @param eventId The unique identifier of the event to mark as processed
     * @throws IllegalArgumentException if eventId is null, empty, or longer than 255 characters
     * @throws RuntimeException if a non-recoverable DynamoDB error occurs
     */
    public void markEventProcessed(String eventId) {
        validateEventId(eventId);
        long expiresAt = calculateExpiresAt();
        try {
            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(Map.of(
                            EVENT_ID_ATTRIBUTE, AttributeValue.builder().s(eventId).build(),
                            EXPIRES_AT_ATTRIBUTE, AttributeValue.builder().n(String.valueOf(expiresAt)).build()
                    ))
                    .conditionExpression(CONDITION_EXPRESSION)
                    .build();
            dynamoDbClient.putItem(request);
            logger.debug("Successfully marked event {} as processed with expiration {}", eventId, expiresAt);
            
        } catch (ConditionalCheckFailedException e) {
            logger.debug("Event {} already exists in table {} - idempotent operation", eventId, tableName);
        } catch (DynamoDbException e) {
            logger.error("Failed to mark event {} as processed: {}", eventId, e.getMessage());
            throw new RuntimeException("Failed to mark event as processed", e);
        }
    }
    
    /**
     * Validates the event ID according to the service requirements.
     * 
     * @param eventId The event ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateEventId(String eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        if (eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be empty or blank");
        }
        if (eventId.length() > MAX_EVENT_ID_LENGTH) {
            throw new IllegalArgumentException("Event ID cannot be longer than " + MAX_EVENT_ID_LENGTH + " characters");
        }
    }
    
    /**
     * Calculates the expiration timestamp for the current time plus TTL days.
     * 
     * @return Unix timestamp in seconds for current time + TTL days
     */
    private long calculateExpiresAt() {
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        return currentTimeSeconds + (ttlDays * SECONDS_PER_DAY);
    }
}
