package com.example.usermanagement.servlets;

import com.example.usermanagement.model.UserRecord;
import com.example.usermanagement.util.DatastoreUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        Map<String, Object> result = new HashMap<>();
        if (email == null || password == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("error", "Missing credentials");
            resp.getWriter().write(gson.toJson(result));
            return;
        }
        UserRecord u = DatastoreUtil.getUserByEmail(email);
        if (u == null || !u.getPassword().equals(password)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("error", "Invalid credentials");
            resp.getWriter().write(gson.toJson(result));
            return;
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("email", email);
        result.put("ok", true);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(result));
    }
}

