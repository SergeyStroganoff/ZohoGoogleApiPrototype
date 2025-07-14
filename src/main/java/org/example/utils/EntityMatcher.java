package org.example.utils;

import org.example.entity.Customer;
import org.example.entity.zoho.comon.Address;
import org.example.entity.zoho.comon.ContactPerson;
import org.example.entity.zoho.contacts.LanguageCode;
import org.example.entity.zoho.contacts.ZohoContactRequest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Example Utility class for creating Zoho contact requests from Customer entities.
 * This class provides a method to convert a Customer object into a ZohoContactRequest,
 * which can be used to create or update contacts in Zoho CRM.
 */

public class EntityMatcher {
    public static ZohoContactRequest createRequest(Customer customer) {
        ZohoContactRequest request = new ZohoContactRequest();
        String contactName = Stream.of(customer.getFirstName(), customer.getSecondName())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" ")).trim();
        request.setContactName(contactName.isEmpty() ? customer.getEmail() : contactName);
        request.setContactType("customer");
        request.setPaymentTerms(15);
        request.setCurrencyId("5971371000000000097");
        request.setLanguageCode(LanguageCode.EN);
        request.setIsTaxable(true);
        request.setTaxId("5971371000000092060");
        request.setTaxAuthorityId("5971371000000092052");

        ContactPerson contactPerson = new ContactPerson();
        contactPerson.setFirstName(customer.getFirstName());
        contactPerson.setLastName(customer.getSecondName());
        contactPerson.setEmail(customer.getEmail());
        contactPerson.setPhone(customer.getPhone());
        request.setContactPersons(List.of(contactPerson));

        AddressParser.parseAndSetAddress(customer);

        Address address = new Address();
        address.setAddressLine1(customer.getAddress());
        address.setCity(customer.getCity());
        address.setState(customer.getState());
        address.setZip(customer.getZipCode());
        request.setBillingAddress(address);
        request.setShippingAddress(address); // Если оба адреса совпадают
        // Notes
        request.setNotes(customer.getNote());
        return request;
    }
}
