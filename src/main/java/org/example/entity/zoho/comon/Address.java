package org.example.entity.zoho.comon;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    private String attention;

    @JsonProperty("address")
    private String addressLine1;

    @JsonProperty("street2")
    private String addressLine2;

    @JsonProperty("state_code")
    private String stateCode;

    private String city;
    private String state;
    private String zip;
    private String country;
    private String fax;
    private String phone;
}
