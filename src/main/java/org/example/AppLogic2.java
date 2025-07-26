package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Customer;
import org.example.entity.google.CalendarEvent;
import org.example.entity.zoho.contacts.ZohoContactRequest;
import org.example.entity.zoho.contacts.ZohoContactResponse;
import org.example.entity.zoho.estimate.LineItem;
import org.example.entity.zoho.estimate.ZohoEstimateRequest;
import org.example.entity.zoho.estimate.ZohoEstimateResponse;
import org.example.processor.GoogleEventParser;
import org.example.service.aws.DynamoDbEventDeduplicationService;
import org.example.service.google.GoogleCalendarService;
import org.example.service.google.GoogleRouteService;
import org.example.service.zoho.ZohoContactService;
import org.example.service.zoho.ZohoEstimateService;
import org.example.utils.EntityMatcher;
import org.example.utils.UTCTimeConverter;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
public class AppLogic2 {
    private static final double KM_TO_MILES_COEFFICIENT = 0.000621371;
    public static final String DELIMITER = "#";
    private final String itemId = "5971371000000098023";
    private final String departureAddress;
    private final GoogleCalendarService calendarService;
    private final GoogleRouteService routeService;
    private final GoogleEventParser eventParser;
    private final ZohoContactService contactService;
    private final ZohoEstimateService estimateService;
    private final String organizationId;
    private final DynamoDbEventDeduplicationService deduplicationService;

    public AppLogic2(ServiceFactory factory) throws IOException, InterruptedException {
        this.departureAddress = factory.getOfficeAddress();
        this.calendarService = factory.getCalendarService();
        this.routeService = factory.getRouteService();
        this.eventParser = new GoogleEventParser();
        this.contactService = factory.getContactService();
        this.estimateService = factory.getEstimateService();
        this.organizationId = factory.getOrganizationId();
        this.deduplicationService = factory.getDeduplicationService();
    }

    public String runSync() throws IOException, InterruptedException {
        String start = UTCTimeConverter.getUTCDateTimeNow();
        String end = UTCTimeConverter.getUTCDateTimeWithOffset(3, ChronoUnit.DAYS);
        log.info("Fetching calendar events: {} to {}", start, end);
        List<CalendarEvent> events = calendarService.getEventsByDate(start, end);
        for (CalendarEvent event : events) {
            if (deduplicationService.isEventProcessed(event.getId())) {
                log.info("Event {} already processed", event.getId());
                continue;
            }
            processEvent(event);
        }
        return "Run complete.";
    }

    private void processEvent(CalendarEvent event) {
        eventParser.retrieveCustomer(event, DELIMITER).ifPresentOrElse(
                customer -> {
                    enrichWithDistance(customer);
                    processZohoFlow(event.getId(), customer);
                },
                () -> log.warn("Customer not found in event: {}", event.getId())
        );
    }

    private void enrichWithDistance(Customer customer) {
        try {
            routeService.getRouteEstimate(departureAddress, customer.getAddress()).ifPresent(route -> {
                var el = route.getRows()[0].getElements()[0];
                double miles = el.getDistance().getValue() * KM_TO_MILES_COEFFICIENT;
                String note = String.format("Distance: %s, %.2f mi, duration: %s",
                        el.getDistance().getText(), miles, el.getDuration().getText());
                customer.setNote(note);
            });
        } catch (IOException | InterruptedException e) {
            log.warn("Could not calculate distance for {}: {}", customer, e.getMessage());
        }
    }

    private void processZohoFlow(String eventId, Customer customer) {
        try {
            ZohoContactRequest contactRequest = EntityMatcher.createContactRequest(customer);
            ZohoContactResponse contactResponse = contactService.addNewContact(contactRequest, organizationId);
            if (contactResponse.getCode() == 0) {
                log.info("Contact added: {}", customer);
                deduplicationService.markEventProcessed(eventId);
                createEstimate(contactResponse.getContact().getContactId(), customer);
            } else {
                log.error("Failed to add contact: {}", contactResponse.getMessage());
            }
        } catch (IOException e) {
            log.error("Zoho contact failure for {}: {}", customer, e.getMessage());
        }
    }

    private void createEstimate(long contactId, Customer customer) throws JsonProcessingException {
        LineItem item = new LineItem();
        item.setItemId(itemId);
        item.setRate(70);
        item.setQuantity(1);

        ZohoEstimateRequest estimate = EntityMatcher.createEstimateRequest(String.valueOf(contactId), List.of(item));
        ZohoEstimateResponse response = estimateService.createEstimate(estimate, organizationId);

        if (response.getCode() == 0) {
            log.info("Estimate created for {}: {}", customer, response.getEstimate().getEstimateId());
        } else {
            log.error("Estimate failed: {}", response.getMessage());
        }
    }
}

