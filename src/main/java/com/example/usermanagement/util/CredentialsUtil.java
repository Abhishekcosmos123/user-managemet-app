package com.example.usermanagement.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CredentialsUtil {
    private static final String CREDENTIALS_FILE = "/mystical-option-484210-n1-70235a3446e8.json";
    private static final String DEFAULT_PROJECT_ID = "mystical-option-484210-n1";
    private static String cachedProjectId = null;
    
    public static String getProjectId() {
        if (cachedProjectId != null) {
            return cachedProjectId;
        }
        
        // Try environment variable first
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId != null && !projectId.isEmpty()) {
            cachedProjectId = projectId;
            return projectId;
        }
        
        // Try config.properties
        projectId = Config.get("GOOGLE_CLOUD_PROJECT", null);
        if (projectId != null && !projectId.isEmpty()) {
            cachedProjectId = projectId;
            return projectId;
        }
        
        // Try reading from JSON file in classpath
        try (InputStream credsStream = CredentialsUtil.class.getResourceAsStream(CREDENTIALS_FILE)) {
            if (credsStream != null) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(credsStream, StandardCharsets.UTF_8));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                String json = jsonBuilder.toString();
                JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
                if (jsonObj.has("project_id")) {
                    projectId = jsonObj.get("project_id").getAsString();
                    cachedProjectId = projectId;
                    return projectId;
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        
        // Default from constant
        cachedProjectId = DEFAULT_PROJECT_ID;
        return DEFAULT_PROJECT_ID;
    }
    
    public static GoogleCredentials getCredentials() throws IOException {
        // First, try environment variable pointing to file system path
        String credsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credsPath != null && !credsPath.isEmpty()) {
            try {
                java.io.File credsFile = new java.io.File(credsPath);
                if (credsFile.exists()) {
                    return GoogleCredentials.fromStream(
                        new java.io.FileInputStream(credsFile)
                    ).createScoped(
                        "https://www.googleapis.com/auth/datastore",
                        "https://www.googleapis.com/auth/bigquery"
                    );
                }
            } catch (Exception e) {
                // Fall through to classpath resource
            }
        }
        
        // Try config.properties for file path
        String configCredsPath = Config.get("GOOGLE_APPLICATION_CREDENTIALS", null);
        IOException lastException = null;
        
        if (configCredsPath != null && !configCredsPath.isEmpty()) {
            // If it starts with /, treat as classpath resource, otherwise as file path
            if (configCredsPath.startsWith("/")) {
                // Classpath resource
                try (InputStream credsStream = CredentialsUtil.class.getResourceAsStream(configCredsPath)) {
                    if (credsStream != null) {
                        return GoogleCredentials.fromStream(credsStream)
                            .createScoped(
                                "https://www.googleapis.com/auth/datastore",
                                "https://www.googleapis.com/auth/bigquery"
                            );
                    } else {
                        lastException = new IOException("Classpath resource not found: " + configCredsPath);
                    }
                } catch (IOException e) {
                    lastException = e;
                }
            } else {
                // File system path
                try {
                    java.io.File credsFile = new java.io.File(configCredsPath);
                    if (credsFile.exists()) {
                        return GoogleCredentials.fromStream(
                            new java.io.FileInputStream(credsFile)
                        ).createScoped(
                            "https://www.googleapis.com/auth/datastore",
                            "https://www.googleapis.com/auth/bigquery"
                        );
                    } else {
                        lastException = new IOException("File not found: " + configCredsPath);
                    }
                } catch (IOException e) {
                    lastException = e;
                }
            }
        }
        
        // Try loading from default classpath resource
        try (InputStream credsStream = CredentialsUtil.class.getResourceAsStream(CREDENTIALS_FILE)) {
            if (credsStream != null) {
                return GoogleCredentials.fromStream(credsStream)
                    .createScoped(
                        "https://www.googleapis.com/auth/datastore",
                        "https://www.googleapis.com/auth/bigquery"
                    );
            } else {
                lastException = new IOException("Classpath resource not found: " + CREDENTIALS_FILE);
            }
        } catch (IOException e) {
            lastException = e;
        }
        
        // If we get here, no credentials were found - throw descriptive error
        String errorMsg = "Failed to load Google Cloud credentials. Tried:\n" +
            "1. Environment variable GOOGLE_APPLICATION_CREDENTIALS: " + 
                (credsPath != null && !credsPath.isEmpty() ? credsPath : "not set") + "\n" +
            "2. Config property GOOGLE_APPLICATION_CREDENTIALS: " + 
                (configCredsPath != null && !configCredsPath.isEmpty() ? configCredsPath : "not set") + "\n" +
            "3. Classpath resource: " + CREDENTIALS_FILE + "\n\n" +
            "Please ensure:\n" +
            "- The JSON credentials file exists in src/main/resources" + CREDENTIALS_FILE + "\n" +
            "- Or set GOOGLE_APPLICATION_CREDENTIALS environment variable pointing to the JSON file\n" +
            "- Or configure GOOGLE_APPLICATION_CREDENTIALS in config.properties (use /path for classpath or absolute path for file system)";
        
        if (lastException != null) {
            throw new IOException(errorMsg + "\nLast error: " + lastException.getMessage(), lastException);
        } else {
            throw new IOException(errorMsg);
        }
    }
}
