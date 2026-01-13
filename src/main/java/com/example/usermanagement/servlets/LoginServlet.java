package com.example.usermanagement.servlets;

import com.example.usermanagement.model.UserRecord;
import com.example.usermanagement.util.DatastoreUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public class LoginServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Please provide both email and password.");
            return;
        }
        
        email = email.trim().toLowerCase();
        
        try {
            UserRecord u = DatastoreUtil.getUserByEmail(email);
            if (u == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("Invalid email or password. User not found.");
                return;
            }
            
            String storedPassword = u.getPassword();
            if (storedPassword == null || !storedPassword.equals(password)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("Invalid email or password.");
                return;
            }
            
            // Login successful
            HttpSession session = req.getSession(true);
            session.setAttribute("email", email);
            session.setAttribute("userName", u.getName() != null ? u.getName() : email);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Login successful! Redirecting...");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

