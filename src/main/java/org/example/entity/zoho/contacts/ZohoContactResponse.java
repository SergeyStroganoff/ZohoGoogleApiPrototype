package org.example.entity.zoho.contacts;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZohoContactResponse {
    private int code;
    private String message;

    @JsonAlias("contact")
    private ZohoContact contact;
}
