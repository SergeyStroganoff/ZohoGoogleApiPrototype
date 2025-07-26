package org.example;

import org.example.service.OAuthTokenRefresher;
import org.example.service.aws.DynamoDbEventDeduplicationService;
import org.example.service.google.GoogleCalendarService;
import org.example.service.google.GoogleRouteService;
import org.example.service.zoho.ZohoContactService;
import org.example.service.zoho.ZohoEstimateService;
import org.example.utils.AwsParameterStoreCredentialsRetriever;
import org.example.utils.CredentialsRetriever;
import org.example.utils.JsonUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

public class ServiceFactory {

    private final HttpClient httpClient;
    private final TokenManager tokenManager;

    public ServiceFactory() {
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        CredentialsRetriever retriever = new AwsParameterStoreCredentialsRetriever(System.getenv("PARAM_NAME"), Region.US_EAST_1, JsonUtils.OBJECT_MAPPER);
        tokenManager = new TokenManager(retriever, new OAuthTokenRefresher(httpClient, JsonUtils.OBJECT_MAPPER));
    }

    public GoogleCalendarService getCalendarService() throws IOException, InterruptedException {
        return new GoogleCalendarService(tokenManager.getGoogleCalendarAccessToken(), httpClient, JsonUtils.OBJECT_MAPPER);
    }

    public GoogleRouteService getRouteService() {
        return new GoogleRouteService(tokenManager.getGoogleMapAPIKey(), httpClient, JsonUtils.OBJECT_MAPPER);
    }

    public ZohoContactService getContactService() throws IOException, InterruptedException {
        return new ZohoContactService(tokenManager.getZOHOInvoiceAccessToken(), httpClient, JsonUtils.OBJECT_MAPPER);
    }

    public ZohoEstimateService getEstimateService() throws IOException, InterruptedException {
        return new ZohoEstimateService(tokenManager.getZOHOInvoiceAccessToken(), httpClient, JsonUtils.OBJECT_MAPPER);
    }

    public String getOrganizationId() {
        return tokenManager.getZOHOInvoiceOrganisationId();
    }

    public DynamoDbEventDeduplicationService getDeduplicationService() {
        return new DynamoDbEventDeduplicationService(
                DynamoDbClient.create(),
                getDynamoDbTableName(),
                getDynamoDbTtlDays()
        );
    }

    public String getOfficeAddress() {
        String address = System.getenv("OFFICE_ADDRESS");
        if (address == null || address.isBlank()) throw new IllegalStateException("OFFICE_ADDRESS is missing");
        return address;
    }

    public String getDynamoDbTableName() {
        String tableName = System.getenv("DYNAMODB_TABLE_NAME");
        if (tableName == null || tableName.isBlank()) throw new IllegalStateException("DYNAMODB_TABLE_NAME is missing");
        return tableName;
    }

    public int getDynamoDbTtlDays() {
        String ttlDays = System.getenv("DYNAMODB_TTL");
        if (ttlDays == null || ttlDays.isBlank()) throw new IllegalStateException("DYNAMODB_TTL is missing");
        try {
            return Integer.parseInt(ttlDays);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid DYNAMODB_TTL value: " + ttlDays, e);
        }
    }
}

