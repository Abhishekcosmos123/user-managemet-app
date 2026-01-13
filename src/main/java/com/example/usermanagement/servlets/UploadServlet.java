package com.example.usermanagement.servlets;

import com.example.usermanagement.model.UserRecord;
import com.example.usermanagement.util.DatastoreUtil;
import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Part filePart = req.getPart("file");
        if (filePart == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing file");
            return;
        }

        try (InputStream in = filePart.getInputStream();
             Workbook workbook = new XSSFWorkbook(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIt = sheet.iterator();
            if (!rowIt.hasNext()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Empty sheet");
                return;
            }
            Row header = rowIt.next();
            List<String> cols = new ArrayList<>();
            for (Cell c : header) {
                cols.add(c.getStringCellValue().trim().toLowerCase());
            }

            int count = 0;
            while (rowIt.hasNext()) {
                Row r = rowIt.next();
                UserRecord u = new UserRecord();
                for (int i = 0; i < cols.size(); i++) {
                    Cell cell = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String val = cell == null ? "" : getCellString(cell);
                    switch (cols.get(i)) {
                        case "name": u.setName(val); break;
                        case "dob": u.setDob(val); break;
                        case "email": u.setEmail(val); break;
                        case "password": u.setPassword(val); break;
                        case "phone": u.setPhone(val); break;
                        case "gender": u.setGender(val); break;
                        case "address": u.setAddress(val); break;
                        default: break;
                    }
                }
                if (u.getEmail() == null || u.getEmail().isEmpty()) continue;
                DatastoreUtil.saveUser(u);
                count++;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("imported", count);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error processing file: " + ex.getMessage());
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }
}

