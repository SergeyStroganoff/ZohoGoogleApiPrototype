package org.example.entity.zoho.comon;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @JsonAlias("first_name")
    private String firstName;

    @JsonAlias("last_name")
    private String lastName;

    private String email;
    private String phone;
    private String mobile;

    @JsonAlias("is_primary_contact")
    private Boolean isPrimaryContact;
}
