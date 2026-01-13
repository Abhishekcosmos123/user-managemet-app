package com.example.usermanagement.servlets;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;
public class SampleExcelServlet extends HttpServlet {
    private static final String[] GENDERS = {"Male","Female","Other"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("users");
            Row header = sheet.createRow(0);
            String[] cols = {"Name","DOB","Email","Password","Phone","Gender","Address"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            Random rnd = new Random(42);
            for (int i = 1; i <= 100; i++) {
                Row r = sheet.createRow(i);
                String name = "User " + i;
                String email = "user" + i + "@example.com";
                String dob = LocalDate.of(1980 + rnd.nextInt(30), 1 + rnd.nextInt(12), 1 + rnd.nextInt(27)).toString();
                String phone = String.format("555-01%03d", i);
                String gender = GENDERS[rnd.nextInt(GENDERS.length)];
                String address = (i) + " Example St, City";
                r.createCell(0).setCellValue(name);
                r.createCell(1).setCellValue(dob);
                r.createCell(2).setCellValue(email);
                r.createCell(3).setCellValue("password" + i);
                r.createCell(4).setCellValue(phone);
                r.createCell(5).setCellValue(gender);
                r.createCell(6).setCellValue(address);
            }

            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-Disposition", "attachment; filename=\"sample-users.xlsx\"");
            wb.write(resp.getOutputStream());
        }
    }
}

