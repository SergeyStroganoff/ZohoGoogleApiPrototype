package org.example.processor;

//todo: распарсить евент извлечь customer
// сходить в базу данных и проверить обработан ли event
// пойти в ZOHO и проверить есть ли такой customer
// если нет, то создать его
// обратиться в гугл map и посчитать расстояние от работы до клиента и обратно
// создать запись пробега в базе данных или Exell

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
    private static final String DESCRIPTION_PREFIX = "Event Name:";

    public Optional<Customer> retrieveCustomer(CalendarEvent calendarEvent, String delimiter) {
        if (calendarEvent == null) {
            logger.debug("CalendarEvent is null !!!");
            return Optional.empty();
        }
        if (calendarEvent.getSummary() == null || calendarEvent.getSummary().isBlank()) {
            logger.debug("CalendarEvent summary is null for event: {}", calendarEvent.getICalUID());
            return Optional.empty();
        }
        Customer customer;
        if (calendarEvent.getSummary().startsWith(delimiter)) {
            customer = parseCustomer(calendarEvent, delimiter);
            return Optional.of(customer);
        }
        if (calendarEvent.getDescription() != null && calendarEvent.getDescription().startsWith(DESCRIPTION_PREFIX)) {
            customer = parseCalendlyCustomer(calendarEvent);
            return Optional.of(customer);
        }
        return Optional.empty();
    }

    private Customer parseCalendlyCustomer(CalendarEvent calendarEvent) {
        String[] summary = calendarEvent.getSummary().split(" ");
        Customer customer = new Customer();
        customer.setFirstName(summary[0]);
        customer.setSecondName(summary[1]);
        customer.setPhone(parseMobilePhone(calendarEvent.getDescription()));
        if (calendarEvent.getAttendees().size() > 1) {
            customer.setEmail(calendarEvent.getAttendees().get(1).getEmail());
        }
        if (calendarEvent.getLocation() != null) {
            customer.setAddress(calendarEvent.getLocation());
        }
        return customer;
    }

    public Customer parseCustomer(CalendarEvent calendarEvent, String delimiter) {
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
