package com.example.usermanagement.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SessionServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Map<String, Object> payload = new HashMap<>();

        if (session == null || session.getAttribute("email") == null) {
            payload.put("loggedIn", false);
        } else {
            payload.put("loggedIn", true);
            payload.put("email", session.getAttribute("email"));
            payload.put("userName", session.getAttribute("userName"));
        }

        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(payload));
    }
}

