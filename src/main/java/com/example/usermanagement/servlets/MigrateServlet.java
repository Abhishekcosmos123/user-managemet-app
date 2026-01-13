package com.example.usermanagement.servlets;

import com.example.usermanagement.model.UserRecord;
import com.example.usermanagement.util.BigQueryUtil;
import com.example.usermanagement.util.DatastoreUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/migrate")
public class MigrateServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String dataset = req.getParameter("dataset");
        String table = req.getParameter("table");
        if (dataset == null || table == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson("Missing dataset or table"));
            return;
        }
        List<UserRecord> users = DatastoreUtil.listUsers();
        BigQueryUtil.ensureTable(dataset, table);
        BigQueryUtil.insertUsers(dataset, table, users);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(Map.of("migrated", users.size())));
    }
}

