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
public class ZohoEstimateResponse {
    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("estimate")
    private Estimate estimate;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Estimate {
        @JsonProperty("estimate_id")
        private String estimateId;

        @JsonProperty("customer_id")
        private String customerId;

        @JsonProperty("line_items")
        private List<LineItem> lineItems;

        @JsonProperty("status")
        private String status;
    }
}
