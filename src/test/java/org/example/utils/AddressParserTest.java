package org.example.utils;

import org.example.entity.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AddressParserTest {

    @Test
    void testParseStandardAddress() {
        Customer customer = new Customer();
        customer.setAddress("2894 E 3rd St, Bloomington, IN 47401");

        AddressParser.parseAndSetAddress(customer);

        assertEquals("2894 E 3rd St", customer.getAddress());
        assertEquals("Bloomington", customer.getCity());
        assertEquals("IN", customer.getState());
        assertEquals("47401", customer.getZipCode());
    }

    @Test
    void testParseAddressWithExtendedZip() {
        Customer customer = new Customer();
        customer.setAddress("123 Main St, Springfield, IL 62704-1234");

        AddressParser.parseAndSetAddress(customer);

        assertEquals("123 Main St", customer.getAddress());
        assertEquals("Springfield", customer.getCity());
        assertEquals("IL", customer.getState());
        assertEquals("62704-1234", customer.getZipCode());
    }

    @Test
    void testParseInvalidAddressFormat() {
        Customer customer = new Customer();
        customer.setAddress("Some Unknown Format Address");

        AddressParser.parseAndSetAddress(customer);

        assertEquals("Some Unknown Format Address", customer.getAddress());
        assertNull(customer.getCity());
        assertNull(customer.getState());
        assertNull(customer.getZipCode());
    }

    @Test
    void testNullAddress() {
        Customer customer = new Customer();
        customer.setAddress(null);

        AddressParser.parseAndSetAddress(customer);

        assertNull(customer.getAddress());
        assertNull(customer.getCity());
        assertNull(customer.getState());
        assertNull(customer.getZipCode());
    }
}

