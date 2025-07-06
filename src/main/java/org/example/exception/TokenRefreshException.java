package org.example.exception;

/**
 * TokenRefreshException is thrown when there is an issue refreshing an OAuth token.
 */
public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String message) {
        super(message);
    }

    public TokenRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
