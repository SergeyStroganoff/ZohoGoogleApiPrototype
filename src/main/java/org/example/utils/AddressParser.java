package org.example.utils;

import org.example.entity.Customer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressParser {

    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "^(.+?),\\s*(.+?),\\s*([A-Z]{2})\\s*(\\d{5}(?:-\\d{4})?)(?:,\\s*(.+))?$"
    );

    private AddressParser() {
    }

    /**
     * Parses a full address string and sets the individual components (street, city, state, zip code)
     * on the provided Customer object.
     *
     * @param customer the Customer object to update with parsed address components
     */
    public static void parseAndSetAddress(Customer customer) {
        String fullAddress = customer.getAddress();
        if (fullAddress == null || fullAddress.isBlank()) return;
        Matcher matcher = ADDRESS_PATTERN.matcher(fullAddress);
        if (matcher.matches()) {
            customer.setAddress(matcher.group(1));
            customer.setCity(matcher.group(2));
            customer.setState(matcher.group(3));
            customer.setZipCode(matcher.group(4));
        }
    }
}

