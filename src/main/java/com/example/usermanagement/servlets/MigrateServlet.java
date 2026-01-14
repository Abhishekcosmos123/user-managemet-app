package com.example.usermanagement.servlets;

import com.example.usermanagement.model.UserRecord;
import com.example.usermanagement.util.BigQueryUtil;
import com.example.usermanagement.util.DatastoreUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MigrateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        
        try {
            String dataset = req.getParameter("dataset");
            String table = req.getParameter("table");
            if (dataset == null || dataset.isEmpty()) {
                dataset = com.example.usermanagement.util.Config.get("DEFAULT_BIGQUERY_DATASET", "user_dataset");
            }
            if (table == null || table.isEmpty()) {
                table = com.example.usermanagement.util.Config.get("DEFAULT_BIGQUERY_TABLE", "User");
            }
            
            List<UserRecord> datastoreUsers = DatastoreUtil.listUsers();
            if (datastoreUsers.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("No users found in Datastore to migrate.");
                return;
            }
            
            // Get existing emails from BigQuery to prevent duplicates
            Set<String> existingEmails;
            try {
                existingEmails = BigQueryUtil.getExistingEmails(dataset, table);
            } catch (Exception e) {
                // If table doesn't exist yet, proceed with all users
                existingEmails = java.util.Collections.emptySet();
            }
            
            final Set<String> finalExistingEmails = existingEmails;
            
            // Filter out users that already exist in BigQuery (based on email)
            List<UserRecord> newUsers = datastoreUsers.stream()
                .filter(user -> {
                    if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                        return false; // Skip users without email
                    }
                    String email = user.getEmail().toLowerCase().trim();
                    return !finalExistingEmails.contains(email);
                })
                .collect(Collectors.toList());
            
            if (newUsers.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("All users from Datastore already exist in BigQuery. No new users to migrate.");
                return;
            }
            
            // Ensure table exists before inserting
            BigQueryUtil.ensureTable(dataset, table);
            
            // Migrate only new users
            BigQueryUtil.insertUsers(dataset, table, newUsers);
            
            int skipped = datastoreUsers.size() - newUsers.size();
            String message = String.format(
                "Successfully migrated %d new user%s to BigQuery (dataset: %s, table: %s). %s",
                newUsers.size(),
                newUsers.size() == 1 ? "" : "s",
                dataset,
                table,
                skipped > 0 ? skipped + " user" + (skipped == 1 ? "" : "s") + " already existed and were skipped." : ""
            );
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Migration interrupted: " + e.getMessage());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

