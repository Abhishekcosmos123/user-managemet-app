package com.example.usermanagement.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CredentialsUtil {

    private static final String CREDENTIALS_FILE = "/mystical-option-484210-n1-70235a3446e8.json";
    private static final String DEFAULT_PROJECT_ID = "test-f5f0e";
    private static String cachedProjectId = null;

    /**
     * Get the Google Cloud project ID
     */
    public static String getProjectId() {
        if (cachedProjectId != null) {
            return cachedProjectId;
        }

        // 1️⃣ Check environment variable
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId != null && !projectId.isEmpty()) {
            cachedProjectId = projectId;
            return projectId;
        }

        // 2️⃣ Check config.properties
        projectId = Config.get("GOOGLE_CLOUD_PROJECT", null);
        if (projectId != null && !projectId.isEmpty()) {
            cachedProjectId = projectId;
            return projectId;
        }

        // 3️⃣ Check JSON file in classpath
        try (InputStream credsStream = CredentialsUtil.class.getResourceAsStream(CREDENTIALS_FILE)) {
            if (credsStream != null) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(credsStream, StandardCharsets.UTF_8));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                JsonObject jsonObj = JsonParser.parseString(jsonBuilder.toString()).getAsJsonObject();
                if (jsonObj.has("project_id")) {
                    projectId = jsonObj.get("project_id").getAsString();
                    cachedProjectId = projectId;
                    return projectId;
                }
            }
        } catch (Exception ignored) {}

        // 4️⃣ Default
        cachedProjectId = DEFAULT_PROJECT_ID;
        return DEFAULT_PROJECT_ID;
    }

    /**
     * Get GoogleCredentials with proper scopes (Datastore + BigQuery + Storage)
     */
    public static GoogleCredentials getCredentials() throws IOException {
        IOException lastException = null;

        // 1️⃣ Try environment variable
        String credsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credsPath != null && !credsPath.isEmpty()) {
            try {
                File file = new File(credsPath);
                if (file.exists()) {
                    return GoogleCredentials.fromStream(new FileInputStream(file))
                            .createScoped(List.of(
                                    "https://www.googleapis.com/auth/datastore",
                                    "https://www.googleapis.com/auth/bigquery",
                                    "https://www.googleapis.com/auth/devstorage.full_control" // Storage
                            ));
                }
            } catch (IOException e) {
                lastException = e;
            }
        }

        // 2️⃣ Try config.properties
        String configCredsPath = Config.get("GOOGLE_APPLICATION_CREDENTIALS", null);
        if (configCredsPath != null && !configCredsPath.isEmpty()) {
            try {
                if (configCredsPath.startsWith("/")) {
                    // Classpath
                    try (InputStream stream = CredentialsUtil.class.getResourceAsStream(configCredsPath)) {
                        if (stream != null) {
                            return GoogleCredentials.fromStream(stream)
                                    .createScoped(List.of(
                                            "https://www.googleapis.com/auth/datastore",
                                            "https://www.googleapis.com/auth/bigquery",
                                            "https://www.googleapis.com/auth/devstorage.full_control"
                                    ));
                        }
                    }
                } else {
                    // File system path
                    File file = new File(configCredsPath);
                    if (file.exists()) {
                        return GoogleCredentials.fromStream(new FileInputStream(file))
                                .createScoped(List.of(
                                        "https://www.googleapis.com/auth/datastore",
                                        "https://www.googleapis.com/auth/bigquery",
                                        "https://www.googleapis.com/auth/devstorage.full_control"
                                ));
                    }
                }
            } catch (IOException e) {
                lastException = e;
            }
        }

        // 3️⃣ Try classpath resource
        try (InputStream stream = CredentialsUtil.class.getResourceAsStream(CREDENTIALS_FILE)) {
            if (stream != null) {
                return GoogleCredentials.fromStream(stream)
                        .createScoped(List.of(
                                "https://www.googleapis.com/auth/datastore",
                                "https://www.googleapis.com/auth/bigquery",
                                "https://www.googleapis.com/auth/devstorage.full_control"
                        ));
            }
        } catch (IOException e) {
            lastException = e;
        }

        // 4️⃣ Fail with descriptive message
        String errorMsg = "Failed to load Google Cloud credentials. Tried:\n" +
                "1. Environment variable GOOGLE_APPLICATION_CREDENTIALS: " + 
                    (credsPath != null ? credsPath : "not set") + "\n" +
                "2. Config property GOOGLE_APPLICATION_CREDENTIALS: " + 
                    (configCredsPath != null ? configCredsPath : "not set") + "\n" +
                "3. Classpath resource: " + CREDENTIALS_FILE + "\n" +
                "Please ensure the JSON file exists and contains valid service account credentials.";

        if (lastException != null) {
            throw new IOException(errorMsg + "\nLast error: " + lastException.getMessage(), lastException);
        } else {
            throw new IOException(errorMsg);
        }
    }
}
