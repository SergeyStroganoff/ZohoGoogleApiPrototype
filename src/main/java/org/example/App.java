package org.example;

import org.example.entity.AppCredentials;
import org.example.utils.AppCredentialsReaderImpl;
import org.example.utils.CredentialsReader;
import org.slf4j.Logger;

public class App {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {

        CredentialsReader credentialsReader = new AppCredentialsReaderImpl();
        String filePath = "src/main/resources/credentials.json";
        AppCredentials credentials = credentialsReader.readCredentials(filePath);
        logger.info("Retrieved Client ID: {}", credentials.getClientId());

        GoogleTokenManager tokenManager = new GoogleTokenManager(
                credentials.getClientId(),
                credentials.getClientSecret(),
                credentials.getRefreshToken()
        );

        String token = tokenManager.getAccessToken();
// Теперь ты можешь использовать этот токен в своих HTTP запросах

    }
}
