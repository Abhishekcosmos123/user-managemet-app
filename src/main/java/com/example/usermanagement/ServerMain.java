package com.example.usermanagement;

import com.example.usermanagement.servlets.DeleteUserServlet;
import com.example.usermanagement.servlets.LoginServlet;
import com.example.usermanagement.servlets.LogoutServlet;
import com.example.usermanagement.servlets.MigrateServlet;
import com.example.usermanagement.servlets.SessionServlet;
import com.example.usermanagement.servlets.BigQueryUserListServlet;
import com.example.usermanagement.servlets.UploadServlet;
import com.example.usermanagement.servlets.UserListServlet;
import com.example.usermanagement.servlets.SampleExcelServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        String portEnv = System.getenv("PORT");
        int port = 8080;
        if (portEnv != null && !portEnv.isEmpty()) {
            try { port = Integer.parseInt(portEnv); } catch (NumberFormatException ignored) {}
        }

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Serve static resources packaged from src/main/webapp
        Resource base = Resource.newClassPathResource("/");
        if (base != null) {
            context.setBaseResource(base);
        }
        
        // Register API servlets FIRST (before static file handler)
        ServletHolder uploadHolder = new ServletHolder(new UploadServlet());
        uploadHolder.getRegistration().setMultipartConfig(new MultipartConfigElement("/tmp", 10 * 1024 * 1024, 10 * 1024 * 1024, 1024 * 1024));
        context.addServlet(uploadHolder, "/upload");
        context.addServlet(new ServletHolder(new UserListServlet()), "/api/users");
        context.addServlet(new ServletHolder(new DeleteUserServlet()), "/api/users/delete");
        context.addServlet(new ServletHolder(new LoginServlet()), "/login");
        context.addServlet(new ServletHolder(new MigrateServlet()), "/api/migrate");
        context.addServlet(new ServletHolder(new SampleExcelServlet()), "/sample/generate");
        context.addServlet(new ServletHolder(new SessionServlet()), "/api/session");
        context.addServlet(new ServletHolder(new LogoutServlet()), "/logout");
        context.addServlet(new ServletHolder(new BigQueryUserListServlet()), "/api/bigquery/users");
        
        // Add a servlet to serve static files (HTML, CSS, JS) - must be last
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                    throws ServletException, IOException {
                String requestURI = req.getRequestURI();
                String path = requestURI;
                
                // Handle root path
                if (path == null || path.equals("/") || path.isEmpty()) {
                    path = "/index.html";
                }
                
                // Don't serve API paths (but allow HTML files with similar names)
                // Only block exact API endpoints, not HTML files
                if (path.startsWith("/api/") || 
                    path.equals("/upload") ||  // API endpoint, not upload.html
                    path.equals("/login") ||   // API endpoint, not login.html
                    path.equals("/logout") ||   // API endpoint, not logout.html
                    path.startsWith("/sample/")) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                
                // Try to load the resource from classpath
                InputStream is = getClass().getResourceAsStream(path);
                if (is == null) {
                    // Try without leading slash
                    is = getClass().getResourceAsStream(path.startsWith("/") ? path.substring(1) : path);
                }
                
                if (is == null) {
                    System.err.println("Resource not found: " + path);
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                
                String contentType = "text/html";
                if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";
                else if (path.endsWith(".json")) contentType = "application/json";
                else if (path.endsWith(".png")) contentType = "image/png";
                else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
                
                resp.setContentType(contentType);
                try (OutputStream os = resp.getOutputStream()) {
                    is.transferTo(os);
                }
            }
        }), "/*");


        server.setHandler(context);
        server.start();
        server.join();
    }
}

