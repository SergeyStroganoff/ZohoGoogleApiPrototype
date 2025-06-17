package org.example.entity.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteMatrixElement {
    private ResultRepresentation distance;    // Информация о расстоянии
    private ResultRepresentation duration;   // Информация о времени в пути
    private String status;       // Статус для конкретной пары адресов
    @NoArgsConstructor
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultRepresentation {
        private String text;      // Текстовое представление времени
        private int value;        // Время в секундах
    }
}
