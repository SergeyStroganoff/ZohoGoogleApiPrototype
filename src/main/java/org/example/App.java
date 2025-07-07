package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Customer;
import org.example.entity.google.CalendarEvent;
import org.example.entity.google.DistanceGoogleMatrix;
import org.example.entity.zoho.comon.Address;
import org.example.entity.zoho.comon.ContactPerson;
import org.example.entity.zoho.contacts.LanguageCode;
import org.example.entity.zoho.contacts.ZohoContactRequest;
import org.example.processor.GoogleEventParser;
import org.example.service.OAuthTokenRefresher;
import org.example.service.google.GoogleCalendarService;
import org.example.service.google.GoogleRouteService;
import org.example.service.zoho.ZohoContactService;
import org.example.utils.*;
import org.slf4j.Logger;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//todo: распарсить евент извлечь customer
// сходить в базу данных и проверить обработан ли event
// пойти в ZOHO и проверить есть ли такой customer
// если нет, то создать его
// обратиться в гуг map и посчитать расстояние от работы до клиента и обратно
// создать запись пробега в базе данных или Excel

public class App {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);
    private static final String TEST_DEPARTURE_ADDRESS = "55 E Michigan St, Indianapolis, IN 46204, USA";

    public static void main(String[] args) throws Exception {
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
        String endDate = UTCTimeConverter.getUTCDateTimeWithOffset(7, ChronoUnit.DAYS);
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
                            Optional<DistanceGoogleMatrix> distanceGoogleMatrix = googleRouteService.getRouteEstimate(TEST_DEPARTURE_ADDRESS, customer.getAddress());
                            if (distanceGoogleMatrix.isPresent()) {
                                String distanceText = distanceGoogleMatrix.get().getRows()[0].getElements()[0].getDistance().getText();
                                String durationText = distanceGoogleMatrix.get().getRows()[0].getElements()[0].getDuration().getText();
                                logger.debug("Distance from {} to {}: {} meters, duration: {} seconds",
                                        TEST_DEPARTURE_ADDRESS, customer.getAddress(), distanceText, durationText);
                                customer.setNote("Distance from " + TEST_DEPARTURE_ADDRESS + " to customer: " + distanceText + ", duration: " + durationText);
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


}
