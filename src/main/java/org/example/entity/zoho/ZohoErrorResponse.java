package org.example.entity.zoho;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an error response from Zoho API.
 * Contains error code and message.
 */
@NoArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZohoErrorResponse {
    private int code;
    private String message;
}
