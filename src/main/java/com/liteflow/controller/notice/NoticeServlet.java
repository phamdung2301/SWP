package com.liteflow.controller.notice;

import com.liteflow.model.notice.Notice;
import com.liteflow.service.notice.NoticeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Servlet for Notice Board operations
 */
@WebServlet("/api/notices/*")
public class NoticeServlet extends HttpServlet {

    private final NoticeService noticeService;

    public NoticeServlet() {
        this.noticeService = new NoticeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            handleGetNotices(request, response);
        } else if (pathInfo.equals("/unread-count")) {
            handleGetUnreadCount(request, response);
        } else if (pathInfo.startsWith("/")) {
            String noticeIDStr = pathInfo.substring(1);
            handleGetNoticeDetail(request, response, noticeIDStr);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (pathInfo.equals("/mark-read")) {
            handleMarkAsRead(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Get notices for employee dashboard
     */
    private void handleGetNotices(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            HttpSession session = request.getSession(false);
            UUID userId = null;

            if (session != null) {
                Object userLoginObj = session.getAttribute("UserLogin");
                if (userLoginObj instanceof UUID) {
                    userId = (UUID) userLoginObj;
                } else if (userLoginObj instanceof String) {
                    try {
                        userId = UUID.fromString((String) userLoginObj);
                    } catch (IllegalArgumentException e) {
                        // Not a UUID string
                    }
                }
            }

            int limit = 10;
            String limitParam = request.getParameter("limit");
            if (limitParam != null) {
                limit = Integer.parseInt(limitParam);
            }

            List<Notice> notices = noticeService.getActiveNoticesForEmployee(userId, limit);

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("notices", convertNoticesToJSON(notices));
            json.put("count", notices.size());

            response.getWriter().write(json.toString());

        } catch (Exception e) {
            System.err.println("❌ Error getting notices: " + e.getMessage());
            e.printStackTrace();

            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }

    /**
     * Get unread notice count
     */
    private void handleGetUnreadCount(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            HttpSession session = request.getSession(false);
            UUID userId = null;

            if (session != null) {
                Object userLoginObj = session.getAttribute("UserLogin");
                if (userLoginObj instanceof UUID) {
                    userId = (UUID) userLoginObj;
                } else if (userLoginObj instanceof String) {
                    try {
                        userId = UUID.fromString((String) userLoginObj);
                    } catch (IllegalArgumentException e) {
                        // Not a UUID string
                    }
                }
            }

            if (userId == null) {
                JSONObject json = new JSONObject();
                json.put("success", true);
                json.put("unreadCount", 0);
                response.getWriter().write(json.toString());
                return;
            }

            int unreadCount = noticeService.getUnreadCount(userId);

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("unreadCount", unreadCount);

            response.getWriter().write(json.toString());

        } catch (Exception e) {
            System.err.println("❌ Error getting unread count: " + e.getMessage());
            e.printStackTrace();

            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }

    /**
     * Get notice detail
     */
    private void handleGetNoticeDetail(HttpServletRequest request, HttpServletResponse response,
                                      String noticeIDStr) throws IOException {

        try {
            UUID noticeID = UUID.fromString(noticeIDStr);
            Notice notice = noticeService.getNoticeByID(noticeID);

            if (notice == null) {
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("error", "Không tìm thấy thông báo");
                response.getWriter().write(json.toString());
                return;
            }

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("notice", convertNoticeToJSON(notice));

            response.getWriter().write(json.toString());

        } catch (Exception e) {
            System.err.println("❌ Error getting notice detail: " + e.getMessage());
            e.printStackTrace();

            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }

    /**
     * Mark notice as read
     */
    private void handleMarkAsRead(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            HttpSession session = request.getSession(false);
            UUID userId = null;

            if (session != null) {
                Object userLoginObj = session.getAttribute("UserLogin");
                if (userLoginObj instanceof UUID) {
                    userId = (UUID) userLoginObj;
                } else if (userLoginObj instanceof String) {
                    try {
                        userId = UUID.fromString((String) userLoginObj);
                    } catch (IllegalArgumentException e) {
                        // Not a UUID string
                    }
                }
            }

            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("error", "Chưa đăng nhập");
                response.getWriter().write(json.toString());
                return;
            }

            String noticeIDStr = request.getParameter("noticeId");
            if (noticeIDStr == null || noticeIDStr.isEmpty()) {
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("error", "Thiếu noticeId");
                response.getWriter().write(json.toString());
                return;
            }

            UUID noticeID = UUID.fromString(noticeIDStr);
            boolean success = noticeService.markNoticeAsRead(noticeID, userId);

            JSONObject json = new JSONObject();
            json.put("success", success);
            if (!success) {
                json.put("error", "Không thể đánh dấu đã đọc");
            }

            response.getWriter().write(json.toString());

        } catch (Exception e) {
            System.err.println("❌ Error marking as read: " + e.getMessage());
            e.printStackTrace();

            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }

    /**
     * Convert notices list to JSON array
     */
    private JSONArray convertNoticesToJSON(List<Notice> notices) {
        JSONArray jsonArray = new JSONArray();

        for (Notice notice : notices) {
            jsonArray.put(convertNoticeToJSON(notice));
        }

        return jsonArray;
    }

    /**
     * Convert single notice to JSON object
     */
    private JSONObject convertNoticeToJSON(Notice notice) {
        JSONObject json = new JSONObject();

        json.put("noticeID", notice.getNoticeID().toString());
        json.put("title", notice.getTitle());
        json.put("content", notice.getContent());
        json.put("noticeType", notice.getNoticeType());
        json.put("noticeTypeLabel", notice.getNoticeTypeLabel());
        json.put("isPinned", notice.getIsPinned());
        json.put("publishedAt", notice.getFormattedPublishedDateTime());
        json.put("publishedDate", notice.getFormattedPublishedDate());
        json.put("viewCount", notice.getViewCount());
        json.put("isRead", notice.getIsRead());

        if (notice.getCreatedByName() != null) {
            json.put("createdByName", notice.getCreatedByName());
        }

        if (notice.getExpiresAt() != null) {
            json.put("expiresAt", notice.getExpiresAt().toString());
        }

        return json;
    }
}
