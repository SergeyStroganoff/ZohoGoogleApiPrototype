package org.example.service.zoho;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.TokenManager;
import org.example.entity.zoho.contacts.ZohoContactRequest;
import org.example.entity.zoho.contacts.ZohoContactResponse;
import org.example.utils.CredentialsFileRetrieverImpl;
import org.example.utils.CredentialsRetriever;
import org.example.utils.JsonUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ZohoContactServiceIntegrationTest {
    public static final String EXPECTED_CONTACT_DETAILS_IN_RESPONSE = "Expected contact details in response";
    private static final String EXPECTED_SUCCESSFUL_RESPONSE_CODE = "Expected successful response code 0";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;

    @Test
    @Tag("integration")
    void integrationTest_AddNewContact() throws Exception {
        //given
        CredentialsRetriever credentialsRetriever = new CredentialsFileRetrieverImpl();
        TokenManager tokenManager = new TokenManager(credentialsRetriever);
        String accessToken = tokenManager.getZOHOInvoiceAccessToken();
        String ORG_ID = tokenManager.getZOHOInvoiceOrganisationId();
        ZohoContactService contactService = new ZohoContactService(accessToken, httpClient, objectMapper);
        String jsonRequest = """
                {
                  "contact_name": "Test Contact",
                  "company_name": "Bowman and Co",
                  "contact_type": "customer",
                  "payment_terms": 15,
                  "currency_id": "5971371000000000097",
                  "website": "www.zylker.org",
                  "custom_fields": [],
                  "billing_address": {
                    "attention": "Mr.John",
                    "address": "4900 Hopyard Rd, Suite 310",
                    "street2": "Suite 310",
                    "state_code": "CA",
                    "city": "Pleasanton",
                    "state": "CA",
                    "zip": 94588,
                    "country": "U.S.A",
                    "fax": 1234,
                    "phone": "1234"
                  },
                  "shipping_address": {
                    "attention": "Mr.John",
                    "address": "4900 Hopyard Rd, Suite 310",
                    "street2": "Suite 310",
                    "state_code": "CA",
                    "city": "Pleasanton",
                    "state": "CA",
                    "zip": 94588,
                    "country": "U.S.A",
                    "fax": 1234,
                    "phone": "1234"
                  },
                  "contact_persons": [{
                    "salutation": "Mr",
                    "first_name": "Will",
                    "last_name": "Smith",
                    "email": "test@zylker.org",
                    "phone": "1234",
                    "mobile": "1234",
                    "is_primary_contact": true
                  }],
                  "language_code": "en",
                  "is_taxable": true,
                  "tax_id": "5971371000000092060",
                  "tax_authority_id": "5971371000000092052",
                  "notes": "Payment option : Through check"
                }
                """;
        ZohoContactRequest request = objectMapper.readValue(jsonRequest, ZohoContactRequest.class);
        //when
        ZohoContactResponse response = contactService.addNewContact(request, ORG_ID);
        //then
        assertNotNull(response);
        assertEquals(0, response.getCode(), EXPECTED_SUCCESSFUL_RESPONSE_CODE);
        assertNotNull(response.getContact(), EXPECTED_CONTACT_DETAILS_IN_RESPONSE);
        //дополнительные проверки полей
        assertEquals("Bowman and Co", response.getContact().getCompanyName());
        assertEquals("www.zylker.org", response.getContact().getWebsite());
        assertEquals("Test Contact", response.getContact().getContactName());
    }
}
