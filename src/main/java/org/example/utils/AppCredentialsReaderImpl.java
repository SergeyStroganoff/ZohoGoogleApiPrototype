package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.AppCredentials;

import java.io.File;
import java.io.IOException;

public class AppCredentialsReaderImpl implements CredentialsReader {
    @Override
    public AppCredentials readCredentials(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), AppCredentials.class);
    }
}
