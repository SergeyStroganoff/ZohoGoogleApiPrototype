package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.google.CalendarEvent;
import org.example.entity.google.DistanceGoogleMatrix;
import org.example.entity.zoho.contacts.ZohoContactRequest;
import org.example.processor.GoogleEventParser;
import org.example.service.OAuthTokenRefresher;
import org.example.service.google.GoogleCalendarService;
import org.example.service.google.GoogleRouteService;
import org.example.service.zoho.ZohoContactService;
import org.example.utils.*;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// сходить в базу данных и проверить обработан ли event
// обратиться в google map и посчитать расстояние от работы до клиента и обратно
// пойти в ZOHO и проверить есть ли такой customer
// если нет, то создать его
// получать адреса от google map
// создать запись пробега в базе данных или Excel

public class App {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);
    private static String testDepartureAddress = "55 E Michigan St, Indianapolis, IN 46204, USA";

    public static void main(String[] args) throws Exception {
        testDepartureAddress = loadOfficeAddress("/office_address.json", JsonUtils.OBJECT_MAPPER);
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        CredentialsRetriever credentialsReader = new CredentialsFileRetrieverImpl();
        OAuthTokenRefresher oAuthTokenRefresher = new OAuthTokenRefresher(httpClient, JsonUtils.OBJECT_MAPPER);
        TokenManager tokenManager = new TokenManager(credentialsReader, oAuthTokenRefresher);
        String googleToken = tokenManager.getGoogleCalendarAccessToken();

        ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;
        GoogleCalendarService googleCalendarService = new GoogleCalendarService(googleToken, httpClient, objectMapper);

        // Получаем события за три дня начиная с сегодняшнего дня
        String startDate = UTCTimeConverter.getUTCDateTimeNow();
        logger.info("Get all events with Start date: {}", startDate);
        String endDate = UTCTimeConverter.getUTCDateTimeWithOffset(3, ChronoUnit.DAYS);
        List<CalendarEvent> events = googleCalendarService.getEventsByDate(startDate, endDate);
        // Print the events as optional
        for (CalendarEvent event : events) {
            System.out.println(event.toString());
            System.out.println("------------------------------");
        }

        String googleMapsApiKey = tokenManager.getGoogleMapAPIKey();
        GoogleRouteService googleRouteService = new GoogleRouteService(googleMapsApiKey, httpClient, objectMapper);

        GoogleEventParser googleEventParser = new GoogleEventParser();
        String organisationId = tokenManager.getZOHOInvoiceOrganisationId();
        String zohoAccessToken = tokenManager.getZOHOInvoiceAccessToken();
        ZohoContactService zohoContactService = new ZohoContactService(zohoAccessToken, httpClient, objectMapper);

        // retrieving all customers and saving to Zoho
        for (CalendarEvent event : events) {
            googleEventParser.retrieveCustomer(event, "#")
                    .ifPresent(customer -> {
                        logger.info("Customer found: {}", customer);
                        try {
                            Optional<DistanceGoogleMatrix> distanceGoogleMatrix = googleRouteService.getRouteEstimate(testDepartureAddress, customer.getAddress());
                            if (distanceGoogleMatrix.isPresent()) {
                                String distanceText = distanceGoogleMatrix.get().getRows()[0].getElements()[0].getDistance().getText();
                                int distanceInMeters = distanceGoogleMatrix.get().getRows()[0].getElements()[0].getDistance().getValue();
                                int distanceInMiles = (int) (distanceInMeters * 0.000621371); // Convert meters to miles
                                String durationText = distanceGoogleMatrix.get().getRows()[0].getElements()[0].getDuration().getText();
                                logger.debug("Distance from {} to {}: {} meters, duration: {} seconds",
                                        testDepartureAddress, customer.getAddress(), distanceText, durationText);
                                customer.setNote("Distance to customer: " + distanceText + "in miles: " + distanceInMiles + ", duration: " + durationText);
                            } else {
                                logger.warn("No distance data found for customer: {} {}", customer.getFirstName(), customer.getSecondName());
                            }
                            ZohoContactRequest zohoContactRequest = EntityMatcher.createRequest(customer);
                            zohoContactService.addNewContact(zohoContactRequest, organisationId);
                            logger.info("Customer {} successfully added to Zoho", customer);
                        } catch (Exception e) {
                            logger.error("Failed to add customer {} to Zoho: {}", customer, e.getMessage());
                        }
                    });
        }
    }

    private static String loadOfficeAddress(String filePath, ObjectMapper objectMapper) {
        InputStream inputStream = App.class.getResourceAsStream(filePath);
        if (inputStream == null) {
            logger.error("File not found: {}", filePath);
            throw new RuntimeException("File not found: " + filePath);
        }
        try {
            Map<String, String> officeAddress = objectMapper.readValue(inputStream, Map.class);
            logger.info("Office address loaded successfully: {}", officeAddress);
            return officeAddress.get("address");
        } catch (Exception e) {
            logger.error("Error loading office address from file: {}", e.getMessage());
            throw new RuntimeException("Error loading office address from file: " + filePath, e);
        }
    }
}
