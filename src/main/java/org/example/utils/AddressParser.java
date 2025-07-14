package org.example.utils;

import org.example.entity.Customer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


//todo // 1. Add unit tests for AddressParser
// 2. Refactor EntityMatcher to use AddressParser for address parsing
// 3. Ensure that AddressParser handles various address formats and edge cases
public class AddressParser {

    // Pattern: "Street, City, ST ZIP"
    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "^(.+?),\\s*(.+?),\\s*([A-Z]{2})\\s*(\\d{5}(?:-\\d{4})?)$"
    );

    private AddressParser() {
    }

    public static void parseAndSetAddress(Customer customer) {
        String fullAddress = customer.getAddress();
        if (fullAddress == null || fullAddress.isBlank()) return;
        Matcher matcher = ADDRESS_PATTERN.matcher(fullAddress);
        if (matcher.matches()) {
            customer.setAddress(matcher.group(1)); // street address
            customer.setCity(matcher.group(2));
            customer.setState(matcher.group(3));
            customer.setZipCode(matcher.group(4));
        }
    }
}

