package org.example.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UTCTimeConverterTest {

    @Test
    void changedUTCDateTimeThenSuccess() {
        //Given
        UTCTimeConverter utcTimeConverter = new UTCTimeConverter();
        //When
        String result = utcTimeConverter.getUTCDateTime(2023, 10, 1, 12, 0, 0, null);
        //Then
        assertNotNull(result);
        assertEquals("2023-10-01T16:00:00Z", result); // Adjust the expected value based on your timezone
    }
}