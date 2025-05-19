package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.example.entity.AppCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class CredentialsFileRetrieverImpl implements CredentialsRetriever {
    public static final String FILE_PATH = "src/main/resources/credentials.json";
    private final Logger logger = LoggerFactory.getLogger(CredentialsFileRetrieverImpl.class);
    private static final ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;

    @Override
    public AppCredentials readCredentials() throws IOException {
        val credentials = objectMapper.readValue(new File(FILE_PATH), AppCredentials.class);
        logger.info("Credentials loaded successfully");
        return credentials;
    }
    @Override
    public void saveCredentials(AppCredentials credentials) {
        try {
            objectMapper.writeValue(new java.io.File(FILE_PATH), credentials);
            logger.info("Credentials updated successfully in file: {}", FILE_PATH);
        } catch (IOException e) {
            logger.error("Error saving new access token to file: {}", e.getMessage());
        }
    }
}
