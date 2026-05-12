package com.liteflow.controller.payroll;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet for Payroll page (paysheet.jsp)
 */
@WebServlet(name = "PayrollPageServlet", urlPatterns = {"/employee/paysheet"})
public class PayrollPageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Set encoding
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");
            
            // Set page attribute for header menu highlighting
            request.setAttribute("page", "paysheet");

            // Forward to the paysheet JSP page
            request.getRequestDispatcher("/employee/paysheet.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error in PayrollPageServlet: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println("<!DOCTYPE html>");
            response.getWriter().println("<html><head><meta charset='UTF-8'><title>Lỗi</title></head>");
            response.getWriter().println("<body><h1>Lỗi khi tải trang bảng lương</h1>");
            response.getWriter().println("<p>" + e.getMessage() + "</p>");
            response.getWriter().println("</body></html>");
            response.getWriter().flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // For any POST requests, redirect to GET
        response.sendRedirect(request.getContextPath() + "/employee/paysheet");
    }
}

