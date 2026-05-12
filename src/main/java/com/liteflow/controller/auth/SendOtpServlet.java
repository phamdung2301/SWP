package com.liteflow.controller.auth;

import com.liteflow.model.auth.User;
import com.liteflow.service.auth.OtpService;
import com.liteflow.service.auth.UserService;
import com.liteflow.service.auth.AuditService;
import com.liteflow.util.MailUtil;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
// imports trimmed after refactor

/**
 * SendOtpServlet: - Gửi OTP cho Signup (session-based) - Gửi OTP cho Login /
 * Reset (DB-based qua OtpService)
 */
@WebServlet("/send-otp")
public class SendOtpServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(SendOtpServlet.class.getName());
    private final UserService userService = new UserService();
    private final OtpService otpService = new OtpService();
    private final AuditService audit = new AuditService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    String email = req.getParameter("email");
        String ip = req.getRemoteAddr();

        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With")) || "1".equals(req.getParameter("ajax"));
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType(isAjax ? "application/json" : "text/html; charset=UTF-8");

    try {
            if (email == null || email.isBlank()) {
                // Fallback to session email if available
                HttpSession s = req.getSession(false);
                if (s != null) {
                    Object ssEmail = s.getAttribute("otpEmail");
                    if (ssEmail instanceof String && !((String) ssEmail).isBlank()) {
                        email = ((String) ssEmail).trim().toLowerCase();
                    }
                }
            }

            if (email == null || email.isBlank()) {
                if (isAjax) {
                    resp.getWriter().write("{\"success\":false,\"msg\":\"Email is required to send OTP\"}");
                    return;
                }
                req.setAttribute("error", "Email is required to send OTP");
                req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
                return;
            }

            email = email.trim().toLowerCase();
            User user = userService.findByEmail(email);

            if (user == null) {
                // ✅ Signup OTP → session-based per authen.sql schema
                String otp = otpService.issueOtpForEmail(email, ip);

                // keep email and OTP in session so verify page can read it
                HttpSession session = req.getSession(true);
                session.setAttribute("otpContext", "signup");
                session.setAttribute("otpEmail", email);
                session.setAttribute("signupOtpCode", otp);
                session.setAttribute("signupOtpExpire", java.time.LocalDateTime.now().plusMinutes(5));

                // Send mail for non-admin (admin uses fixed code handled in verify logic)
                if (!"admin@liteflow.com".equalsIgnoreCase(email)) {
                    // Send asynchronously to speed up AJAX response
                    final String finalEmail = email;
                    final String finalOtp = otp;
                    new Thread(() -> {
                        try {
                            MailUtil.sendOtpMail(finalEmail, finalOtp);
                        } catch (Exception ex) {
                            LOG.warning("Failed to send OTP email async: " + ex.getMessage());
                        }
                    }).start();
                } else {
                    LOG.info("🔑 Admin fixed OTP: 000000");
                }

                audit.log(null,
                        AuditService.AuditAction.OTP_ISSUED,
                        AuditService.ObjectType.USER,
                        email,
                        "OTP issued for signup (session-based)",
                        ip);

                String msg = "✅ OTP has been sent to " + email;
                if (isAjax) {
                    resp.getWriter().write("{\"success\":true,\"msg\":\"" + msg.replaceAll("\"","\\\"") + "\"}");
                } else {
                    req.setAttribute("msg", msg);
                    req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
                }

            } else {
                // ✅ Login/Reset OTP → DB-based
                String otp = otpService.issueOtp(user, ip);

                // ensure otpEmail saved so verify-otp page admin box can read it
                HttpSession session = req.getSession(true);
                session.setAttribute("otpEmail", email);

                if (!"admin@liteflow.com".equalsIgnoreCase(email)) {
                    // Send asynchronously to speed up AJAX response
                    final String finalEmail = email;
                    final String finalOtp = otp;
                    new Thread(() -> {
                        try {
                            MailUtil.sendOtpMail(finalEmail, finalOtp);
                        } catch (Exception ex) {
                            LOG.warning("Failed to send OTP email async: " + ex.getMessage());
                        }
                    }).start();
                } else {
                    LOG.info("🔑 Admin fixed OTP: 000000");
                }

                audit.log(user,
                        AuditService.AuditAction.OTP_ISSUED,
                        AuditService.ObjectType.USER,
                        user.getUserID().toString(),
                        "OTP issued for login/reset",
                        ip);

                String msg = "✅ OTP has been sent to " + email;
                if (isAjax) {
                    resp.getWriter().write("{\"success\":true,\"msg\":\"" + msg.replaceAll("\"","\\\"") + "\"}");
                } else {
                    req.setAttribute("msg", msg);
                    req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
                }
            }
        } catch (Exception ex) {
            // Unexpected server error: if AJAX request, return JSON error to avoid HTML pages
            ex.printStackTrace();
            String err = "Server error. Please try again later.";
            if (isAjax) {
                resp.getWriter().write("{\"success\":false,\"msg\":\"" + err.replaceAll("\"","\\\"") + "\"}");
            } else {
                req.setAttribute("error", err);
                req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
            }
        }
    }
}
