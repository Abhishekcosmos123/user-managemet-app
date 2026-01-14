package com.example.usermanagement.util;

import com.example.usermanagement.model.UserRecord;
import com.google.cloud.bigquery.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.FieldValueList;

public class BigQueryUtil {
    private static final BigQuery bigquery = createBigQuery();
    private static final Gson gson = new GsonBuilder().create();

    private static BigQuery createBigQuery() {
        try {
            String projectId = CredentialsUtil.getProjectId();
            GoogleCredentials credentials = CredentialsUtil.getCredentials();
            return BigQueryOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize BigQuery: " + e.getMessage(), e);
        }
    }

    public static void ensureTable(String datasetName, String tableName) {
        Dataset dataset = bigquery.getDataset(datasetName);
        if (dataset == null) {
            bigquery.create(DatasetInfo.newBuilder(datasetName).build());
        }
        TableId tableId = TableId.of(datasetName, tableName);
        Table table = bigquery.getTable(tableId);
        if (table == null) {
            Schema schema = Schema.of(
                    Field.of("name", StandardSQLTypeName.STRING),
                    Field.of("dob", StandardSQLTypeName.STRING),
                    Field.of("email", StandardSQLTypeName.STRING),
                    Field.of("password", StandardSQLTypeName.STRING),
                    Field.of("phone", StandardSQLTypeName.STRING),
                    Field.of("gender", StandardSQLTypeName.STRING),
                    Field.of("address", StandardSQLTypeName.STRING)
            );
            TableDefinition def = StandardTableDefinition.of(schema);
            TableInfo tInfo = TableInfo.newBuilder(tableId, def).build();
            bigquery.create(tInfo);
        }
    }

    /**
     * Batch load users to BigQuery using load job (instead of streaming insert).
     * This works with BigQuery free tier.
     */
    public static void insertUsers(String datasetName, String tableName, List<UserRecord> users) throws IOException, InterruptedException {
        if (users == null || users.isEmpty()) {
            return;
        }

        TableId tableId = TableId.of(datasetName, tableName);
        
        // Create temporary JSON file with newline-delimited JSON (NDJSON)
        File tempFile = null;
        try {
            // Create temp file
            tempFile = File.createTempFile("bigquery_load_", "_" + UUID.randomUUID() + ".json");
            
            // Write users as newline-delimited JSON
            try (FileWriter writer = new FileWriter(tempFile)) {
                for (UserRecord u : users) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("name", u.getName() != null ? u.getName() : "");
                    row.put("dob", u.getDob() != null ? u.getDob() : "");
                    row.put("email", u.getEmail() != null ? u.getEmail() : "");
                    row.put("password", u.getPassword() != null ? u.getPassword() : "");
                    row.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    row.put("gender", u.getGender() != null ? u.getGender() : "");
                    row.put("address", u.getAddress() != null ? u.getAddress() : "");
                    
                    writer.write(gson.toJson(row));
                    writer.write("\n");
                }
            }
            
            // Upload to GCS first (required for batch load)
            String bucketName = "dataset_1_user";
            String objectName = "users_" + UUID.randomUUID() + ".json";
            String gcsUri = "gs://" + bucketName + "/" + objectName;
            
            // Ensure bucket exists and upload file
            com.google.cloud.storage.Storage storage = com.google.cloud.storage.StorageOptions.newBuilder()
                .setProjectId(CredentialsUtil.getProjectId())
                .setCredentials(CredentialsUtil.getCredentials())
                .build()
                .getService();
            
            // Create bucket if it doesn't exist
            com.google.cloud.storage.Bucket bucket = storage.get(bucketName);
            if (bucket == null) {
                bucket = storage.create(com.google.cloud.storage.BucketInfo.newBuilder(bucketName).build());
            }
            
            // Upload file to GCS
            bucket.create(objectName, Files.readAllBytes(tempFile.toPath()), "application/json");
            
            // Create load job configuration pointing to GCS
            LoadJobConfiguration loadConfig = LoadJobConfiguration.newBuilder(tableId, gcsUri)
                .setFormatOptions(FormatOptions.json())
                .setWriteDisposition(JobInfo.WriteDisposition.WRITE_APPEND) // Append to existing data
                .setCreateDisposition(JobInfo.CreateDisposition.CREATE_NEVER) // Table must exist
                .build();
            
            // Create and run the load job
            JobId jobId = JobId.of(UUID.randomUUID().toString());
            Job loadJob = bigquery.create(JobInfo.newBuilder(loadConfig).setJobId(jobId).build());
            
            // Wait for job to complete
            loadJob = loadJob.waitFor();
            
            // Check for errors
            if (loadJob.getStatus().getError() != null) {
                throw new RuntimeException("BigQuery load job failed: " + 
                    loadJob.getStatus().getError().toString());
            }
            
            // Check for job errors
            if (loadJob.getStatus().getExecutionErrors() != null && 
                !loadJob.getStatus().getExecutionErrors().isEmpty()) {
                throw new RuntimeException("BigQuery load job execution errors: " + 
                    loadJob.getStatus().getExecutionErrors());
            }
            
            // Clean up GCS object after load
            try {
                storage.delete(com.google.cloud.storage.BlobId.of(bucketName, objectName));
            } catch (Exception e) {
                // Ignore cleanup errors
            }
            
        } finally {
            // Clean up temp file
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    /**
     * List all users from BigQuery table
     */
    public static List<UserRecord> listUsers(String datasetName, String tableName) throws IOException, InterruptedException {
        List<UserRecord> users = new ArrayList<>();
        
        String query = String.format(
            "SELECT name, dob, email, password, phone, gender, address FROM `%s.%s.%s` ORDER BY email",
            CredentialsUtil.getProjectId(), datasetName, tableName
        );
        
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());
        
        // Wait for query to complete
        queryJob = queryJob.waitFor();
        
        if (queryJob.getStatus().getError() != null) {
            throw new RuntimeException("BigQuery query failed: " + queryJob.getStatus().getError().toString());
        }
        
        // Get results
        TableResult result = queryJob.getQueryResults();
        for (FieldValueList row : result.iterateAll()) {
            UserRecord user = new UserRecord();
            user.setName(getFieldValue(row, "name"));
            user.setDob(getFieldValue(row, "dob"));
            user.setEmail(getFieldValue(row, "email"));
            user.setPassword(getFieldValue(row, "password"));
            user.setPhone(getFieldValue(row, "phone"));
            user.setGender(getFieldValue(row, "gender"));
            user.setAddress(getFieldValue(row, "address"));
            users.add(user);
        }
        
        return users;
    }

    /**
     * Get set of existing email addresses from BigQuery table (for duplicate checking)
     */
    public static java.util.Set<String> getExistingEmails(String datasetName, String tableName) throws IOException, InterruptedException {
        java.util.Set<String> emails = new java.util.HashSet<>();
        
        try {
            String query = String.format(
                "SELECT DISTINCT email FROM `%s.%s.%s` WHERE email IS NOT NULL",
                CredentialsUtil.getProjectId(), datasetName, tableName
            );
            
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            JobId jobId = JobId.of(UUID.randomUUID().toString());
            Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());
            
            // Wait for query to complete
            queryJob = queryJob.waitFor();
            
            if (queryJob.getStatus().getError() != null) {
                // If table doesn't exist or is empty, return empty set
                return emails;
            }
            
            // Get results
            TableResult result = queryJob.getQueryResults();
            for (FieldValueList row : result.iterateAll()) {
                String email = getFieldValue(row, "email");
                if (email != null && !email.isEmpty()) {
                    emails.add(email.toLowerCase().trim());
                }
            }
        } catch (Exception e) {
            // If query fails (e.g., table doesn't exist), return empty set
            // This allows first migration to proceed
        }
        
        return emails;
    }

    private static String getFieldValue(FieldValueList row, String fieldName) {
        try {
            if (row.get(fieldName).isNull()) {
                return null;
            }
            return row.get(fieldName).getStringValue();
        } catch (Exception e) {
            return null;
        }
    }
}

