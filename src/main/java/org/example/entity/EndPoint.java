package org.example.entity;

public enum EndPoint {
    ZOHO_INVOICE("https://www.zohoapis.com/invoice/v3/"),
    ZOHO_TOKEN_REFRESH("https://accounts.zoho.com/oauth/v2/token"),
    GOOGLE_TOKEN_REFRESH("https://oauth2.googleapis.com/token"),
    GOOGLE_SHEETS("https://sheets.googleapis.com/v4/spreadsheets/"),
    GOOGLE_CALENDAR("https://www.googleapis.com/calendar/v3/"),
    GOOGLE_CONTACTS("https://people.googleapis.com/v1/"),
    GOOGLE_GMAIL("https://gmail.googleapis.com/gmail/v1/"),
    GOOGLE_AUTH("https://www.googleapis.com/auth/"),
    ZOHO_AUTH("https://www.zohoapis.com/auth/");


    private final String url;

    EndPoint(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
