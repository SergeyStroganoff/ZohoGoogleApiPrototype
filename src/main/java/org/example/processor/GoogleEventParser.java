package org.example.processor;

//todo: распарсить евент извлечь customer
// сходить в базу данных и проверить обработан ли event
// пойти в ZOHO и проверить есть ли такой customer
// если нет, то создать его
// обратиться в гугл map и посчитать расстояние от работы до клиента и обратно
// создать запись пробега в базе данных или Excel

import org.example.entity.Customer;
import org.example.entity.google.CalendarEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for parsing Google Calendar events.
 */
public class GoogleEventParser {
    private static final Logger logger = LoggerFactory.getLogger(GoogleEventParser.class);
    private static final String DESCRIPTION_PREFIX = "Event Name";
    public static final String CALENDAR_EVENT_IS_NULL = "CalendarEvent is null !!!";

    public Optional<Customer> retrieveCustomer(CalendarEvent calendarEvent, String delimiter) {
        if (calendarEvent == null) {
            logger.debug(CALENDAR_EVENT_IS_NULL);
            return Optional.empty();
        }
        if (calendarEvent.getSummary() == null || calendarEvent.getSummary().isBlank()) {
            logger.debug("CalendarEvent summary is null for event: {}", calendarEvent.getICalUID());
            return Optional.empty();
        }
        Customer customer;
        if (calendarEvent.getSummary().startsWith(delimiter)) {
            customer = parseCustomer(calendarEvent);
            return Optional.of(customer);
        }
        if (calendarEvent.getDescription() != null && calendarEvent.getDescription().startsWith(DESCRIPTION_PREFIX)) {
            customer = parseCalendlyCustomer(calendarEvent);
            return Optional.of(customer);
        }
        return Optional.empty();
    }

    /**
     * Parses a Calendly customer from a CalendarEvent.
     *
     * @param calendarEvent The CalendarEvent to parse.
     * @return A Customer object with parsed details.
     */
    private Customer parseCalendlyCustomer(CalendarEvent calendarEvent) {
        String[] summary = calendarEvent.getSummary().split(" ");
        Customer customer = new Customer();
        customer.setFirstName(summary[0]);
        customer.setSecondName(summary[1]);
        customer.setPhone(parseMobilePhone(calendarEvent.getDescription()));
        // todo: check
        if (calendarEvent.getAttendees().size() > 1 && (!calendarEvent.getAttendees().get(1).isOrganizer())) {
            customer.setEmail(calendarEvent.getAttendees().get(1).getEmail());
        }
        if (calendarEvent.getLocation() != null) {
            customer.setAddress(calendarEvent.getLocation());
        }
        return customer;
    }

    /**
     * Parses a customer from a CalendarEvent based on the summary format.
     *
     * @param calendarEvent The CalendarEvent to parse.
     * @return A Customer object with parsed details.
     */
    public Customer parseCustomer(CalendarEvent calendarEvent) {
        String[] parts = calendarEvent.getSummary().split(" ");
        Customer customer = new Customer();
        customer.setFirstName(parts[1]);
        customer.setSecondName(parts[2]);
        String phone = parseMobilePhone(calendarEvent.getSummary());
        if (!phone.isBlank()) {
            customer.setPhone(phone);
        }
        if (calendarEvent.getLocation() != null && !calendarEvent.getLocation().isBlank()) {
            customer.setAddress(calendarEvent.getLocation());
        }
        return customer;
    }

    public String parseMobilePhone(String text) {
        // Regex pattern to match various phone number formats
        String phonePattern =
                "(\\+\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}|" +  // +1 812-929-2381 or similar
                        "\\d{3}[- ]?\\d{3}[- ]?\\d{4}";                                // 812-929-2381 or similar
        Pattern pattern = Pattern.compile(phonePattern);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
