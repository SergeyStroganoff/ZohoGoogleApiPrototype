package org.example.entity.zoho.comon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a contact person in Zoho.
 * This class is used to map the JSON response from Zoho's API for contact persons.
 */
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactPerson {
    private String salutation;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;
    private String phone;
    private String mobile;

    @JsonProperty("is_primary_contact")
    private Boolean isPrimaryContact;
}
