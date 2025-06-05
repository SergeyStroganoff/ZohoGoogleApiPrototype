package org.example.entity.google;

public enum GoogleMatrixResponseStatus {
    OK("OK"),
    INVALID_REQUEST("INVALID_REQUEST"),
    MAX_ELEMENTS_EXCEEDED("MAX_ELEMENTS_EXCEEDED"),
    OVER_QUERY_LIMIT("OVER_QUERY_LIMIT"),
    REQUEST_DENIED("REQUEST_DENIED"),
    UNKNOWN_ERROR("UNKNOWN_ERROR"),
    UNKNOWN_STATUS(null);  // Для случаев, если Google добавит новый статус

    private final String value;

    GoogleMatrixResponseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public static GoogleMatrixResponseStatus fromValue(String value) {
        for (GoogleMatrixResponseStatus status : values()) {
            if (status.value != null && status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return UNKNOWN_STATUS;  // Возвращаем UNKNOWN_STATUS, если значение не найдено
    }
}
