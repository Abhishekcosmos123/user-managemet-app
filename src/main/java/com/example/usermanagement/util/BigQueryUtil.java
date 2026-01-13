package com.example.usermanagement.util;

import com.example.usermanagement.model.UserRecord;
import com.google.cloud.bigquery.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BigQueryUtil {
    private static final BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

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

    public static void insertUsers(String datasetName, String tableName, List<UserRecord> users) {
        TableId tableId = TableId.of(datasetName, tableName);
        List<InsertAllRequest.RowToInsert> rows = new ArrayList<>();
        for (UserRecord u : users) {
            Map<String, Object> content = new HashMap<>();
            content.put("name", u.getName());
            content.put("dob", u.getDob());
            content.put("email", u.getEmail());
            content.put("password", u.getPassword());
            content.put("phone", u.getPhone());
            content.put("gender", u.getGender());
            content.put("address", u.getAddress());
            rows.add(InsertAllRequest.RowToInsert.of(content));
        }
        InsertAllResponse resp = bigquery.insertAll(InsertAllRequest.newBuilder(tableId).setRows(rows).build());
        if (resp.hasErrors()) {
            // For simplicity: throw a runtime exception to make errors visible
            throw new RuntimeException("BigQuery insert errors: " + resp.getInsertErrors());
        }
    }
}

