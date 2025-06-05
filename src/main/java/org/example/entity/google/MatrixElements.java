package org.example.entity.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.maps.model.Distance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatrixElements {
    private Distance distance;    // Информация о расстоянии
    private Duration duration;   // Информация о времени в пути
    private String status;       // Статус для конкретной пары адресов

    @NoArgsConstructor
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Duration {
        private String text;      // Текстовое представление времени
        private int value;        // Время в секундах
    }
}
