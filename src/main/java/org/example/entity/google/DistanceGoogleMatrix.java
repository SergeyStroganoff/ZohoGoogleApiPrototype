package org.example.entity.google;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the response from the Google Distance Matrix API.
 * Contains information about distances and durations between origins and destinations.
 */
@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistanceGoogleMatrix {
    @JsonAlias("destination_addresses")
    private String[] destinationAddresses;
    @JsonAlias("origin_addresses")
    private String[] originAddresses;
    @JsonAlias("rows")
    private GoogleMatrixRow[] rows;  // Changed field name and added @JsonAlias
    /**
     * Status of the response.
     * Indicates whether the request was successful or if there were errors.
     */
    private GoogleMatrixStatus status;

    public void setDestinationAddresses(String[] destinationAddresses) {
        this.destinationAddresses = destinationAddresses;
    }

    public void setOriginAddresses(String[] originAddresses) {
        this.originAddresses = originAddresses;
    }

    public void setRows(GoogleMatrixRow[] rows) {
        this.rows = rows;
    }

    @JsonSetter("status")
    public void setStatus(String status) {
        this.status = GoogleMatrixStatus.fromValue(status);
    }
}


