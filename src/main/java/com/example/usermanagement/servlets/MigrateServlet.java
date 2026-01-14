package com.example.usermanagement.servlets;

import com.example.usermanagement.model.UserRecord;
import com.example.usermanagement.util.BigQueryUtil;
import com.example.usermanagement.util.DatastoreUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
public class MigrateServlet extends HttpServlet {
    private static final Gson gson = new Gson();

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
            
            List<UserRecord> users = DatastoreUtil.listUsers();
            if (users.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("No users found in Datastore to migrate.");
                return;
            }
            
            BigQueryUtil.ensureTable(dataset, table);
            BigQueryUtil.insertUsers(dataset, table, users);
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Successfully migrated " + users.size() + " users to BigQuery dataset: " + dataset + ", table: " + table);
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

