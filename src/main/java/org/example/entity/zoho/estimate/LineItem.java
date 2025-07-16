package org.example.entity.zoho.estimate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineItem {
    @JsonProperty("item_id")
    private String itemId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("rate")
    private double rate;
    @JsonProperty("quantity")
    private int quantity;
}
