package org.example.exception;

/**
 * Custom exception class for handling errors related to Zoho service operations.
 * This class extends RuntimeException and provides constructors for different use cases.
 */
public class ZohoServiceException extends RuntimeException {

    public ZohoServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZohoServiceException(String message) {
        super(message);
    }

}
