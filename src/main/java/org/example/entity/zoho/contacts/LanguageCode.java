package org.example.entity.zoho.contacts;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing language codes used in Zoho Contacts.
 * Each enum constant corresponds to a specific language code.
 */
public enum LanguageCode {
    DE("de"),
    EN("en"),
    ES("es"),
    FR("fr"),
    IT("it"),
    JA("ja"),
    NL("nl"),
    PT("pt"),
    SV("sv"),
    ZH("zh");

    private final String code;

    LanguageCode(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}

