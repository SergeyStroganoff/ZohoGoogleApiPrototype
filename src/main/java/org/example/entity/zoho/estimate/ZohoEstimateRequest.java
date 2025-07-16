package org.example.entity.zoho.estimate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZohoEstimateRequest {
    @JsonProperty("customer_id")
    private String customerId;
    @JsonProperty("line_items")
    private List<LineItem> lineItems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineItem {
        @JsonProperty("item_id")
        private String itemId;
        @JsonProperty("name")
        private double rate;
        @JsonProperty("rate")
        private int quantity;
    }
}
