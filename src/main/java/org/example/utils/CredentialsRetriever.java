package org.example.utils;

import org.example.entity.AppCredentials;

import java.io.IOException;

/**
 * Interface for retrieving and saving application credentials.
 */
public interface CredentialsRetriever {
    /**
     * Reads the application credentials from a specified file.
     *
     * @return the application credentials
     * @throws IOException if an error occurs while reading the file
     */
    AppCredentials readCredentials() throws IOException;

    /**
     * Saves the application credentials to a specified file.
     *
     * @param credentials the application credentials to save
     */
    void saveCredentials(AppCredentials credentials);
}
