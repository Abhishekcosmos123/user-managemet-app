package com.example.usermanagement.servlets;

import com.example.usermanagement.model.UserRecord;
import com.example.usermanagement.util.DatastoreUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/users")
public class UserListServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<UserRecord> users = DatastoreUtil.listUsers();
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(users));
    }
}

