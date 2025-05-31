package org.example.processor;

import lombok.val;
import org.example.entity.Customer;
import org.example.service.GoogleCalendarService;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


class GoogleEventParserTest {
    private final GoogleEventParser googleEventParser = new GoogleEventParser();
    private final GoogleCalendarService googleCalendarService = new GoogleCalendarService("test_token");

    private final String delimiter = "#";

    private final String calandyDescription = "Event Name: Diagnostic visit\\n\\nHi there!\\n\\nIn our service area, we offer diagnostic services. \\n\\nLocation: 11111 W Ratliff Rd\\n\\nPlease share the appliance type and model to help us better prepare for our visit.: Tractor Model DW80US\\n\\nPlease describe the issue.: Turns on but won't run. Address says Spencer but that's just the post office that delivers the mail.\\n\\nIf convenient for you, please provide a phone number so we can connect before the visit.: +1 312-922-2388\\n\\nNeed to make changes to this event?\\nCancel: https://calendly.com/cancellations/20b46974\\nReschedule: https://calendly.com/reschedulings/6974\\n\\nPowered by Calendly.com\\n\",";

    //todo: распарсить евент извлечь customer
    @Test
    void testRetrieve() {
        //given
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("calendar_test.json")) {
            if (inputStream == null) {
                throw new RuntimeException("File not found!");
            }
            String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            val calendarEvent = googleCalendarService.parseEvents(jsonString);
            //when
            Optional<Customer> resultCustomerManualInputOpt = googleEventParser.retrieveCustomer(calendarEvent.get(0), delimiter);
            Optional<Customer> resultCustomerCalendyEventOpt = googleEventParser.retrieveCustomer(calendarEvent.get(2), delimiter);
            //then
            assertAll(
                    () -> assertTrue(resultCustomerManualInputOpt.isPresent(), "Manual input customer should be present"),
                    () -> assertTrue(resultCustomerCalendyEventOpt.isPresent(), "Calendy customer should be present"),

                    () -> {
                        Customer customerManual = resultCustomerManualInputOpt.get();
                        assertAll(
                                () -> assertEquals("Alex", customerManual.getFirstName(), "Manual first name"),
                                () -> assertEquals("Farabaugh", customerManual.getSecondName(), "Manual last name"),
                                () -> assertEquals("400-942-5598", customerManual.getPhone(), "Manual phone"),
                                () -> assertEquals("1601 Willow Road, Menlo Park, CA 94025", customerManual.getAddress(), "Manual address")
                        );
                    },

                    () -> {
                        Customer customerCalandy = resultCustomerCalendyEventOpt.get();
                        assertAll(
                                () -> assertEquals("John", customerCalandy.getFirstName(), "Calendy first name"),
                                () -> assertEquals("Kit", customerCalandy.getSecondName(), "Calendy last name"),
                                () -> assertEquals("+1 912-929-0001", customerCalandy.getPhone(), "Calendy phone"),
                                () -> assertEquals("92 W Ratliff Rd, Liusville, IN 47460, США", customerCalandy.getAddress(), "Calendy address"),
                                () -> assertEquals("johns@yahoo.com", customerCalandy.getEmail(), "Calendy email")
                        );
                    }
            );
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage(), e);
        }
    }

    //todo: parse customer from summary
    @Test
    void whenParseMobilePhoneReturnCorrectPhone() {
        //given
        String expectedMobilePhone = "+1 312-922-2388";
        String description = calandyDescription;
        //when
        String resultMobilePhone = googleEventParser.parseMobilePhone(description);
        assertEquals(expectedMobilePhone, resultMobilePhone);
    }

    //todo: parse customer from summary
    @Test
    void testParseCalendyCustomerReturnCustomer() {

    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme