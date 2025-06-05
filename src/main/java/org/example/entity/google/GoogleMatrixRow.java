package org.example.entity.google;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a row in the Google Matrix API response.
 * Each row corresponds to a specific origin and contains elements for each destination.
 */
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleMatrixRow {
    @JsonAlias("elements")
    private MatrixElements[] elements;
}
