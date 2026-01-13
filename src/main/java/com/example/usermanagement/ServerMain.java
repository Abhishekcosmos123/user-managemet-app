package com.example.usermanagement;

import com.example.usermanagement.servlets.DeleteUserServlet;
import com.example.usermanagement.servlets.LoginServlet;
import com.example.usermanagement.servlets.MigrateServlet;
import com.example.usermanagement.servlets.UploadServlet;
import com.example.usermanagement.servlets.UserListServlet;
import com.example.usermanagement.servlets.SampleExcelServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

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
        ServletHolder defaultHolder = new ServletHolder("default", DefaultServlet.class);
        defaultHolder.setInitParameter("dirAllowed", "false");
        context.addServlet(defaultHolder, "/");

        // Register servlets programmatically
        context.addServlet(new ServletHolder(new UploadServlet()), "/upload");
        context.addServlet(new ServletHolder(new UserListServlet()), "/api/users");
        context.addServlet(new ServletHolder(new DeleteUserServlet()), "/api/users/delete");
        context.addServlet(new ServletHolder(new LoginServlet()), "/login");
        context.addServlet(new ServletHolder(new MigrateServlet()), "/api/migrate");
        context.addServlet(new ServletHolder(new SampleExcelServlet()), "/sample/generate");

        server.setHandler(context);
        server.start();
        server.join();
    }
}

