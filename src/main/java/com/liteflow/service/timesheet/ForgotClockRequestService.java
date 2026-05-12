package com.liteflow.service.timesheet;

import com.liteflow.dao.timesheet.ForgotClockRequestDAO;
import com.liteflow.model.timesheet.ForgotClockRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ForgotClockRequestService {

    private final ForgotClockRequestDAO forgotClockRequestDAO = new ForgotClockRequestDAO();

    /**
     * Lấy tất cả yêu cầu quên chấm công của employee
     */
    public List<ForgotClockRequest> getForgotClockRequestsByEmployeeId(UUID employeeId) {
        return forgotClockRequestDAO.findByEmployeeId(employeeId);
    }

    /**
     * Lấy yêu cầu quên chấm công theo status
     */
    public List<ForgotClockRequest> getForgotClockRequestsByStatus(UUID employeeId, String status) {
        return forgotClockRequestDAO.findByEmployeeAndStatus(employeeId, status);
    }

    /**
     * Lấy yêu cầu quên chấm công theo ID
     */
    public Optional<ForgotClockRequest> getForgotClockRequestById(UUID requestId) {
        ForgotClockRequest request = forgotClockRequestDAO.findById(requestId);
        return Optional.ofNullable(request);
    }

    /**
     * Lấy tất cả yêu cầu đang chờ duyệt
     */
    public List<ForgotClockRequest> getAllPendingRequests() {
        return forgotClockRequestDAO.findPendingRequests();
    }

    /**
     * Lấy yêu cầu trong khoảng thời gian
     */
    public List<ForgotClockRequest> getForgotClockRequestsByDateRange(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        return forgotClockRequestDAO.findByEmployeeAndDateRange(employeeId, startDate, endDate);
    }

    /**
     * Tạo mới yêu cầu quên chấm công
     */
    public boolean createForgotClockRequest(ForgotClockRequest request) {
        if (request == null || request.getEmployee() == null) {
            return false;
        }

        // Validate required fields
        if (request.getForgotDate() == null) {
            throw new IllegalArgumentException("Forgot date is required");
        }

        if (request.getForgotType() == null || request.getForgotType().isEmpty()) {
            throw new IllegalArgumentException("Forgot type is required");
        }

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required");
        }

        // Validate forgot date is in the past
        if (!request.getForgotDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày quên chấm công phải là ngày trong quá khứ");
        }

        // Validate forgot date is not too old (max 7 days ago)
        LocalDate maxAllowedDate = LocalDate.now().minusDays(7);
        if (request.getForgotDate().isBefore(maxAllowedDate)) {
            throw new IllegalArgumentException("Chỉ có thể báo quên chấm công trong vòng 7 ngày gần đây");
        }

        // Validate forgot type
        if (!isValidForgotType(request.getForgotType())) {
            throw new IllegalArgumentException("Invalid forgot type");
        }

        // Check if already has a request for this date
        boolean hasExisting = forgotClockRequestDAO.hasRequestForDate(
                request.getEmployee().getEmployeeID(),
                request.getForgotDate(),
                null
        );

        if (hasExisting) {
            throw new IllegalArgumentException("Bạn đã có yêu cầu cho ngày này");
        }

        // Set default status
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            request.setStatus("Chờ duyệt");
        }

        return forgotClockRequestDAO.insert(request);
    }

    /**
     * Cập nhật yêu cầu quên chấm công
     */
    public boolean updateForgotClockRequest(ForgotClockRequest request) {
        if (request == null || request.getForgotClockRequestId() == null) {
            return false;
        }

        // Validate dates if being updated
        if (request.getForgotDate() != null) {
            if (!request.getForgotDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày quên chấm công phải là ngày trong quá khứ");
            }

            LocalDate maxAllowedDate = LocalDate.now().minusDays(7);
            if (request.getForgotDate().isBefore(maxAllowedDate)) {
                throw new IllegalArgumentException("Chỉ có thể báo quên chấm công trong vòng 7 ngày gần đây");
            }
        }

        // Validate forgot type if being updated
        if (request.getForgotType() != null && !isValidForgotType(request.getForgotType())) {
            throw new IllegalArgumentException("Invalid forgot type");
        }

        return forgotClockRequestDAO.update(request);
    }

    /**
     * Hủy yêu cầu quên chấm công
     */
    public boolean cancelForgotClockRequest(UUID requestId, UUID employeeId) {
        // Verify ownership
        if (!forgotClockRequestDAO.isOwner(requestId, employeeId)) {
            return false;
        }

        Optional<ForgotClockRequest> requestOpt = getForgotClockRequestById(requestId);
        if (requestOpt.isEmpty()) {
            return false;
        }

        ForgotClockRequest request = requestOpt.get();

        // Only allow canceling pending requests
        if (!"Chờ duyệt".equals(request.getStatus())) {
            return false;
        }

        request.setStatus("Đã hủy");
        return forgotClockRequestDAO.update(request);
    }

    /**
     * Xóa yêu cầu quên chấm công
     */
    public boolean deleteForgotClockRequest(UUID requestId) {
        return forgotClockRequestDAO.delete(requestId);
    }

    /**
     * Kiểm tra employee có phải owner không
     */
    public boolean isOwner(UUID requestId, UUID employeeId) {
        return forgotClockRequestDAO.isOwner(requestId, employeeId);
    }

    /**
     * Đếm số yêu cầu đang chờ duyệt của employee
     */
    public long countPendingRequests(UUID employeeId) {
        return forgotClockRequestDAO.countPendingByEmployee(employeeId);
    }

    // ==============================
    // Helper methods
    // ==============================

    private boolean isValidForgotType(String forgotType) {
        return "CHECK_IN".equals(forgotType) || 
               "CHECK_OUT".equals(forgotType) || 
               "BOTH".equals(forgotType);
    }
}

