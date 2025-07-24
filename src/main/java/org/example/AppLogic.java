package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Customer;
import org.example.entity.google.CalendarEvent;
import org.example.entity.google.DistanceGoogleMatrix;
import org.example.entity.zoho.contacts.ZohoContactRequest;
import org.example.entity.zoho.contacts.ZohoContactResponse;
import org.example.entity.zoho.estimate.LineItem;
import org.example.entity.zoho.estimate.ZohoEstimateRequest;
import org.example.entity.zoho.estimate.ZohoEstimateResponse;
import org.example.processor.GoogleEventParser;
import org.example.service.OAuthTokenRefresher;
import org.example.service.aws.DynamoDbEventDeduplicationService;
import org.example.service.google.GoogleCalendarService;
import org.example.service.google.GoogleRouteService;
import org.example.service.zoho.ZohoContactService;
import org.example.service.zoho.ZohoEstimateService;
import org.example.utils.*;
import org.slf4j.Logger;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

// сходить в базу данных и проверить обработан ли event
// обратиться в google map и посчитать расстояние от работы до клиента и обратно
// пойти в ZOHO и проверить есть ли такой customer
// если нет, то создать его
// получать адреса от google map
// создать запись пробега в базе данных или Excel

public class AppLogic {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AppLogic.class);
    public static final String ITEM_ID = "5971371000000098023";
    public static final double KM_TO_MILES_COEFFICIENT = 0.000621371;
    public static final String TABLE_NAME = "ProcessedGoogleCalendarEvents_ZohoIntegration";
    public static final int TTL_DAYS = 30;


    public String runSync() throws Exception {
        String testDepartureAddress = getOfficeAddress();
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        CredentialsRetriever credentialsReader = new CredentialsFileRetrieverImpl();
        OAuthTokenRefresher oAuthTokenRefresher = new OAuthTokenRefresher(httpClient, JsonUtils.OBJECT_MAPPER);
        TokenManager tokenManager = new TokenManager(credentialsReader, oAuthTokenRefresher);


        ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;

        String googleToken = tokenManager.getGoogleCalendarAccessToken();
        GoogleCalendarService googleCalendarService = new GoogleCalendarService(googleToken, httpClient, objectMapper);


        String googleMapsApiKey = tokenManager.getGoogleMapAPIKey();
        GoogleRouteService googleRouteService = new GoogleRouteService(googleMapsApiKey, httpClient, objectMapper);
        GoogleEventParser googleEventParser = new GoogleEventParser();

        String organisationId = tokenManager.getZOHOInvoiceOrganisationId();
        String zohoAccessToken = tokenManager.getZOHOInvoiceAccessToken();
        ZohoContactService zohoContactService = new ZohoContactService(zohoAccessToken, httpClient, objectMapper);
        ZohoEstimateService zohoEstimateService = new ZohoEstimateService(zohoAccessToken, httpClient, objectMapper);

        DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        DynamoDbEventDeduplicationService dynamoDbEventDeduplicationService = new DynamoDbEventDeduplicationService(
                dynamoDbClient, TABLE_NAME, TTL_DAYS // TTL in days
        );

        // main logic
        // Получаем события за три дня начиная с сегодняшнего дня
        String startDate = UTCTimeConverter.getUTCDateTimeNow();
        logger.info("Get all events with Start date: {}", startDate);
        String endDate = UTCTimeConverter.getUTCDateTimeWithOffset(3, ChronoUnit.DAYS);
        List<CalendarEvent> events = googleCalendarService.getEventsByDate(startDate, endDate);
        // Print the events as optional
        for (CalendarEvent event : events) {
            System.out.println(event.getId());
            System.out.println(event.getSummary());
            System.out.println("------------------------------");
        }
        // retrieving all customers and saving to Zoho
        for (CalendarEvent event : events) {
            // Check if the event is already processed
            if (dynamoDbEventDeduplicationService.isEventProcessed(event.getId())) {
                logger.info("Event {} has already been processed, skipping...", event.getId());
                continue; // Skip already processed events
            }

            Optional<Customer> optionalCustomer = googleEventParser.retrieveCustomer(event, "#");
            if (optionalCustomer.isPresent()) {
                Customer customer = optionalCustomer.get();
                logger.info("Customer found: {}", customer);
                try {
                    Optional<DistanceGoogleMatrix> distanceGoogleMatrix = googleRouteService.getRouteEstimate(testDepartureAddress, customer.getAddress());
                    if (distanceGoogleMatrix.isPresent()) {
                        String distanceText = distanceGoogleMatrix.get().getRows()[0].getElements()[0].getDistance().getText();
                        int distanceInMeters = distanceGoogleMatrix.get().getRows()[0].getElements()[0].getDistance().getValue();
                        double distanceInMiles = (distanceInMeters * KM_TO_MILES_COEFFICIENT); // Convert meters to miles
                        String durationText = distanceGoogleMatrix.get().getRows()[0].getElements()[0].getDuration().getText();
                        logger.debug("Distance from {} to {}: {} meters, duration: {} seconds", testDepartureAddress, customer.getAddress(), distanceText, durationText);
                        String note = String.format("Distance to customer: %s km, %.2f miles, duration: %s", distanceText, distanceInMiles, durationText);
                        customer.setNote(note);
                    } else {
                        logger.warn("No distance data found for customer: {} {}", customer.getFirstName(), customer.getSecondName());
                    }
                    ZohoContactRequest zohoContactRequest = EntityMatcher.createContactRequest(customer);
                    ZohoContactResponse zohoContactResponse = zohoContactService.addNewContact(zohoContactRequest, organisationId);
                    if (zohoContactResponse.getCode() == 0) {
                        logger.info("Customer {} successfully added to Zoho", customer);
                        dynamoDbEventDeduplicationService.markEventProcessed(event.getId());
                        // Optionally, create an estimate for the customer
                        LineItem service = new LineItem();
                        service.setItemId(ITEM_ID); // Example item ID
                        service.setRate(70.00); // Example rate
                        service.setQuantity(1); // Example quantity
                        ZohoEstimateRequest estimateRequest = EntityMatcher.createEstimateRequest(String.valueOf(zohoContactResponse.getContact().getContactId()), List.of(service));
                        ZohoEstimateResponse zohoEstimateResponse = zohoEstimateService.createEstimate(estimateRequest, organisationId);
                        if (zohoEstimateResponse.getCode() == 0) {
                            logger.info("Estimate for customer {} created successfully: {}", customer, zohoEstimateResponse.getEstimate().getEstimateId());
                        } else {
                            logger.error("Failed to create estimate for customer {}: {}", customer, zohoEstimateResponse.getMessage());
                        }
                    } else {
                        logger.error("Failed to add customer {} to Zoho: {}", customer, zohoContactResponse.getMessage());
                    }
                } catch (Exception e) {
                    logger.error("Failed to add customer {} to Zoho: {}", customer, e.getMessage());
                }
            }
        }
        dynamoDbClient.close();
        return "Processing completed successfully.";
    }

    private static String getOfficeAddress() {
        String departureAddress = System.getenv("OFFICE_ADDRESS");
        if (departureAddress == null || departureAddress.isBlank()) {
            throw new IllegalStateException("OFFICE_ADDRESS environment variable is missing");
        }
        return departureAddress;
    }

}
