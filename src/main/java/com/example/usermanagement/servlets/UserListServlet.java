package com.example.usermanagement.servlets;

import com.example.usermanagement.model.UserRecord;
import com.example.usermanagement.util.DatastoreUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class UserListServlet extends HttpServlet {
    private static final Gson gson = new Gson();
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        
        // Get query parameters
        String pageParam = req.getParameter("page");
        String pageSizeParam = req.getParameter("pageSize");
        String searchParam = req.getParameter("search");
        
        int page = 1;
        int pageSize = DEFAULT_PAGE_SIZE;
        
        try {
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Math.max(1, Integer.parseInt(pageParam));
            }
            if (pageSizeParam != null && !pageSizeParam.isEmpty()) {
                pageSize = Math.min(MAX_PAGE_SIZE, Math.max(1, Integer.parseInt(pageSizeParam)));
            }
        } catch (NumberFormatException e) {
            // Use defaults if parsing fails
        }
        
        // Get all users from Datastore
        List<UserRecord> allUsers = DatastoreUtil.listUsers();
        
        // Apply search filter if provided
        List<UserRecord> filteredUsers = allUsers;
        if (searchParam != null && !searchParam.trim().isEmpty()) {
            String searchLower = searchParam.toLowerCase().trim();
            filteredUsers = allUsers.stream()
                .filter(u -> {
                    String name = (u.getName() != null ? u.getName() : "").toLowerCase();
                    String email = (u.getEmail() != null ? u.getEmail() : "").toLowerCase();
                    String phone = (u.getPhone() != null ? u.getPhone() : "").toLowerCase();
                    return name.contains(searchLower) || email.contains(searchLower) || phone.contains(searchLower);
                })
                .collect(Collectors.toList());
        }
        
        // Calculate pagination
        int totalItems = filteredUsers.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        // Get paginated slice (guard against out-of-range page)
        List<UserRecord> paginatedUsers;
        if (totalItems == 0 || startIndex >= totalItems) {
            paginatedUsers = List.of();
            page = 1;
            startIndex = 0;
            endIndex = 0;
            totalPages = totalItems == 0 ? 0 : totalPages;
        } else {
            paginatedUsers = filteredUsers.subList(startIndex, endIndex);
        }
        
        // Build response JSON
        JsonObject response = new JsonObject();
        response.add("users", gson.toJsonTree(paginatedUsers));
        response.addProperty("page", page);
        response.addProperty("pageSize", pageSize);
        response.addProperty("totalItems", totalItems);
        response.addProperty("totalPages", totalPages);
        response.addProperty("hasNext", page < totalPages);
        response.addProperty("hasPrev", page > 1);
        
        resp.getWriter().write(gson.toJson(response));
    }
}

