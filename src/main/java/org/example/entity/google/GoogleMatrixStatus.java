package org.example.entity.google;

public enum GoogleMatrixStatus {
    OK("OK"),
    INVALID_REQUEST("INVALID_REQUEST"),
    MAX_ELEMENTS_EXCEEDED("MAX_ELEMENTS_EXCEEDED"),
    OVER_QUERY_LIMIT("OVER_QUERY_LIMIT"),
    REQUEST_DENIED("REQUEST_DENIED"),
    UNKNOWN_ERROR("UNKNOWN_ERROR"),
    UNKNOWN_STATUS(null);  // Для случаев, если Google добавит новый статус

    private final String value;

    GoogleMatrixStatus(String value) {
        this.value = value;
    }

    public static GoogleMatrixStatus fromValue(String value) {
        for (GoogleMatrixStatus status : values()) {
            if (status.value != null && status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return UNKNOWN_STATUS;  // Возвращаем UNKNOWN_STATUS, если значение не найдено
    }

    public String getValue() {
        return value;
    }
}
