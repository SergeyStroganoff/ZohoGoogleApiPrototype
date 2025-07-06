package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.AppCredentials;
import org.example.exception.CredentialsRetrieverRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

import java.io.IOException;

/**
 * AwsParameterStoreCredentialsRetriever is a class that retrieves and saves application credentials from AWS Systems Manager Parameter Store.
 * It uses the AWS SDK for Java to interact with the Parameter Store.
 * If no parameter name is provided or null, it defaults to "/prod/calendar-zoho/credentials".
 */

public class AwsParameterStoreCredentialsRetriever implements CredentialsRetriever {
    public static final String CREDENTIALS_UPDATE_FAIL_MESSAGE = "Failed to save credentials to AWS Parameter Store";
    private final Logger logger = LoggerFactory.getLogger(AwsParameterStoreCredentialsRetriever.class);
    public static final String DEFAULT_PARAMETER_NAME = "/prod/calendar-zoho/credentials";
    private final String parameterName;
    private final SsmClient ssmClient;
    private final ObjectMapper objectMapper;

    public AwsParameterStoreCredentialsRetriever(String parameterName, Region region, ObjectMapper objectMapper) {
        if (parameterName == null || parameterName.isEmpty()) {
            this.parameterName = DEFAULT_PARAMETER_NAME; // Default parameter name if none provided
        } else this.parameterName = parameterName;
        this.ssmClient = SsmClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create()) // Uses default credentials provider chain for AWS Lambda
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AppCredentials readCredentials() throws IOException {
        GetParameterRequest getRequest = GetParameterRequest.builder()
                .name(parameterName)
                .withDecryption(true)
                .build();
        GetParameterResponse getResponse = ssmClient.getParameter(getRequest);
        String json = getResponse.parameter().value();
        if (json == null || json.isEmpty()) {
            logger.error("No credentials found in Parameter Store for parameter: {}", parameterName);
            throw new CredentialsRetrieverRuntimeException("No credentials found in Parameter Store");
        } else {
            logger.info("Credentials retrieved successfully from Parameter Store for parameter: {}", parameterName);
        }
        return objectMapper.readValue(json, AppCredentials.class);
    }

    @Override
    public void updateCredentials(AppCredentials credentials) {
        try {
            String json = objectMapper.writeValueAsString(credentials);
            PutParameterRequest putRequest = PutParameterRequest.builder()
                    .name(parameterName)
                    .value(json)
                    .type(ParameterType.SECURE_STRING)
                    .overwrite(true)
                    .build();
            ssmClient.putParameter(putRequest);
        } catch (Exception e) {
            logger.error(CREDENTIALS_UPDATE_FAIL_MESSAGE + " {}", e.getMessage());
            throw new CredentialsRetrieverRuntimeException(CREDENTIALS_UPDATE_FAIL_MESSAGE, e);
        }
    }
}
