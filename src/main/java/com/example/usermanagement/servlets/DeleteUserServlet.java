package com.example.usermanagement.servlets;

import com.example.usermanagement.util.DatastoreUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public class DeleteUserServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        Map<String, Object> result = new HashMap<>();
        if (id == null || id.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("error", "Missing id");
            resp.getWriter().write(gson.toJson(result));
            return;
        }
        DatastoreUtil.deleteUserById(id);
        result.put("deleted", id);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(result));
    }
}

