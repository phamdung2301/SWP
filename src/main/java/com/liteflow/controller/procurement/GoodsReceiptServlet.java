package com.liteflow.controller.procurement;

import com.liteflow.service.procurement.ProcurementService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

@WebServlet(urlPatterns = {"/procurement/gr"})
public class GoodsReceiptServlet extends HttpServlet {
    private final ProcurementService service = new ProcurementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, jakarta.servlet.ServletException {
       
        req.getRequestDispatcher("/procurement/goods-receipt.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID poid = UUID.fromString(req.getParameter("poid"));
        String userLogin = (String) req.getSession().getAttribute("UserLogin");
        UUID user = userLogin != null ? UUID.fromString(userLogin) : null;
        String notes = req.getParameter("notes");
        service.receivePartial(poid, user, notes);
        resp.sendRedirect(req.getContextPath() + "/procurement/gr?status=ok");
    }
}
