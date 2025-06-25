package org.example.exception;

public class CredentialsRetrieverException extends RuntimeException {
    public CredentialsRetrieverException(String message, Throwable cause) {
        super(message, cause);
    }
}
