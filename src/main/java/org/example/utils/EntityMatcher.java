package org.example.utils;

import org.example.entity.Customer;
import org.example.entity.zoho.comon.Address;
import org.example.entity.zoho.comon.ContactPerson;
import org.example.entity.zoho.contacts.LanguageCode;
import org.example.entity.zoho.contacts.ZohoContactRequest;
import org.example.entity.zoho.estimate.LineItem;
import org.example.entity.zoho.estimate.ZohoEstimateRequest;

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
    public static ZohoContactRequest createContactRequest(Customer customer) {
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

    /**
     * Creates a ZohoEstimateRequest from a customer ID and a list of line items.
     *
     * @param customerId The ID of the customer for whom the estimate is created.
     * @param lineItems  The list of line items to be included in the estimate.
     * @return A ZohoEstimateRequest object containing the customer ID and line items.
     */
    public static ZohoEstimateRequest createEstimateRequest(String customerId, List<LineItem> lineItems) {
        ZohoEstimateRequest request = new ZohoEstimateRequest();
        request.setCustomerId(customerId);
        request.setLineItems(lineItems);
        return request;
    }
}
