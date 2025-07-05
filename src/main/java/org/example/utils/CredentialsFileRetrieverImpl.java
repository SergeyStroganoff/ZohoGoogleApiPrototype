package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.example.entity.AppCredentials;
import org.example.exception.CredentialsRetrieverRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * CredentialsFileRetrieverImpl is a class that retrieves and saves application credentials from resource a JSON file.
 * Writes the credentials to a file in the src/main/resources (the approach is not recommended for production and jar file).
 */
public class CredentialsFileRetrieverImpl implements CredentialsRetriever {

    public static final String FILE_PATH = "src/main/resources/credentials.json";
    public static final String CREDENTIALS_JSON = "/credentials.json";
    public static final String CREDENTIALS_LOADED_SUCCESSFULLY = "Credentials loaded successfully";
    public static final String CREDENTIALS_UPDATED_SUCCESSFULLY_MESSAGE = "Credentials updated successfully in file: {}";
    public static final String ERROR_SAVING_UPDATED_TOKEN = "Error saving new access token to file: {}";
    private final Logger logger = LoggerFactory.getLogger(CredentialsFileRetrieverImpl.class);
    private static final ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;

    @Override
    public AppCredentials readCredentials() throws IOException {

        try (InputStream inputStream = CredentialsFileRetrieverImpl.class.getResourceAsStream(CREDENTIALS_JSON)) {
            if (inputStream == null) {
                logger.error("File not found: {}", CREDENTIALS_JSON);
                throw new CredentialsRetrieverRuntimeException("File not found !");
            }

            val credentials = objectMapper.readValue(inputStream, AppCredentials.class);
            logger.info(CREDENTIALS_LOADED_SUCCESSFULLY);
            return credentials;
        }
    }
    @Override
    public void saveCredentials(AppCredentials credentials) {
        try {
            objectMapper.writeValue(new java.io.File(FILE_PATH), credentials);
            logger.info(CREDENTIALS_UPDATED_SUCCESSFULLY_MESSAGE, FILE_PATH);
        } catch (IOException e) {
            logger.error(ERROR_SAVING_UPDATED_TOKEN, e.getMessage());
        }
    }
}
