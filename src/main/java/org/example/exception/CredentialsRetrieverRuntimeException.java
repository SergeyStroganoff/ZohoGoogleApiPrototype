package org.example.exception;

public class CredentialsRetrieverRuntimeException extends RuntimeException {
    public CredentialsRetrieverRuntimeException(String message) {
        super(message);
    }

    public CredentialsRetrieverRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
