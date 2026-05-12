package com.liteflow.controller.employee;

import com.liteflow.model.auth.Employee;
import com.liteflow.service.employee.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SetupEmployeeServlet", urlPatterns = {"/employee/setup"})
public class SetupEmployeeServlet extends HttpServlet {

    private EmployeeService employeeService;

    @Override
    public void init() throws ServletException {
        employeeService = new EmployeeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Set encoding
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");
            
            // Set page attribute for header menu highlighting
            request.setAttribute("page", "setup-employee");

            // Load employees list
            try {
                List<Employee> employees = employeeService.getAllEmployees();
                request.setAttribute("employees", employees);
            } catch (Exception e) {
                System.err.println("Error loading employees: " + e.getMessage());
                e.printStackTrace();
                request.setAttribute("employees", new java.util.ArrayList<Employee>());
            }

            // Forward to the setup employee JSP page
            request.getRequestDispatcher("/employee/setupEmployee.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error in SetupEmployeeServlet: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Lỗi khi tải trang: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // For any POST requests, redirect to GET
        response.sendRedirect(request.getContextPath() + "/employee/setup");
    }
}
