package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    private String id;
    private String firstName;
    private String secondName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
}
