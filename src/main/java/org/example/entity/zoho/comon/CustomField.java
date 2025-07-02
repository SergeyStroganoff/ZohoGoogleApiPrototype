package org.example.entity.zoho.comon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a custom field in Zoho.
 * This class is used to map the JSON response from Zoho's API for custom fields.
 */
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomField {
    private String value;
    private Integer index;
    private String label;
}
