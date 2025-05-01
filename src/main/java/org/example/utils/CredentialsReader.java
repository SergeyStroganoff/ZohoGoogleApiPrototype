package org.example.utils;

import org.example.entity.AppCredentials;

import java.io.IOException;

public interface CredentialsReader {
    AppCredentials readCredentials(String filePath) throws IOException;
}
