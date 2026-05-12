package com.liteflow.service.timesheet;

import com.liteflow.dao.timesheet.LeaveRequestDAO;
import com.liteflow.model.timesheet.LeaveRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LeaveRequestService {

    private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();

    /**
     * Lấy tất cả đơn xin nghỉ của employee
     */
    public List<LeaveRequest> getLeaveRequestsByEmployeeId(UUID employeeId) {
        return leaveRequestDAO.findByEmployeeId(employeeId);
    }

    /**
     * Lấy đơn xin nghỉ trong khoảng thời gian
     */
    public List<LeaveRequest> getLeaveRequestsByDateRange(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        return leaveRequestDAO.findByEmployeeAndDateRange(employeeId, startDate, endDate);
    }

    /**
     * Lấy đơn xin nghỉ theo status
     */
    public List<LeaveRequest> getLeaveRequestsByStatus(UUID employeeId, String status) {
        return leaveRequestDAO.findByEmployeeAndStatus(employeeId, status);
    }

    /**
     * Lấy đơn xin nghỉ theo ID
     */
    public Optional<LeaveRequest> getLeaveRequestById(UUID leaveRequestId) {
        LeaveRequest request = leaveRequestDAO.findById(leaveRequestId);
        return Optional.ofNullable(request);
    }

    /**
     * Tạo mới đơn xin nghỉ
     */
    public boolean createLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest == null || leaveRequest.getEmployee() == null) {
            return false;
        }

        // Validate dates
        if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }

        // Calculate total days
        long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        leaveRequest.setTotalDays(BigDecimal.valueOf(days));

        // Check for overlapping leave requests
        boolean hasOverlap = leaveRequestDAO.hasOverlappingLeave(
                leaveRequest.getEmployee().getEmployeeID(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                null
        );

        if (hasOverlap) {
            throw new IllegalArgumentException("Bạn đã có đơn xin nghỉ trùng thời gian này");
        }

        // Set default status
        if (leaveRequest.getStatus() == null || leaveRequest.getStatus().isEmpty()) {
            leaveRequest.setStatus("Chờ duyệt");
        }

        return leaveRequestDAO.insert(leaveRequest);
    }

    /**
     * Cập nhật đơn xin nghỉ
     */
    public boolean updateLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest == null || leaveRequest.getLeaveRequestId() == null) {
            return false;
        }

        // Validate dates if they're being updated
        if (leaveRequest.getStartDate() != null && leaveRequest.getEndDate() != null) {
            if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
                throw new IllegalArgumentException("End date must be after or equal to start date");
            }

            // Recalculate total days
            long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
            leaveRequest.setTotalDays(BigDecimal.valueOf(days));

            // Check for overlapping leave requests (excluding current request)
            boolean hasOverlap = leaveRequestDAO.hasOverlappingLeave(
                    leaveRequest.getEmployee().getEmployeeID(),
                    leaveRequest.getStartDate(),
                    leaveRequest.getEndDate(),
                    leaveRequest.getLeaveRequestId()
            );

            if (hasOverlap) {
                throw new IllegalArgumentException("Bạn đã có đơn xin nghỉ trùng thời gian này");
            }
        }

        return leaveRequestDAO.update(leaveRequest);
    }

    /**
     * Hủy đơn xin nghỉ (chỉ employee có thể hủy đơn của mình)
     */
    public boolean cancelLeaveRequest(UUID leaveRequestId, UUID employeeId) {
        Optional<LeaveRequest> requestOpt = getLeaveRequestById(leaveRequestId);
        if (requestOpt.isEmpty()) {
            return false;
        }

        LeaveRequest request = requestOpt.get();

        // Check ownership
        if (!request.getEmployee().getEmployeeID().equals(employeeId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn này");
        }

        // Only pending requests can be cancelled
        if (!"Chờ duyệt".equals(request.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể hủy đơn đang chờ duyệt");
        }

        request.setStatus("Đã hủy");
        return leaveRequestDAO.update(request);
    }

    /**
     * Xóa đơn xin nghỉ
     */
    public boolean deleteLeaveRequest(UUID leaveRequestId) {
        return leaveRequestDAO.delete(leaveRequestId);
    }

    /**
     * Kiểm tra quyền sở hữu
     */
    public boolean isOwner(UUID leaveRequestId, UUID employeeId) {
        Optional<LeaveRequest> request = getLeaveRequestById(leaveRequestId);
        if (request.isEmpty()) {
            return false;
        }
        return request.get().getEmployee().getEmployeeID().equals(employeeId);
    }

    /**
     * Lấy tất cả đơn xin nghỉ đang chờ duyệt (for managers)
     */
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestDAO.findPendingRequests();
    }

    /**
     * Duyệt đơn xin nghỉ (for managers)
     */
    public boolean approveLeaveRequest(UUID leaveRequestId, UUID reviewerId, String reviewNotes) {
        Optional<LeaveRequest> requestOpt = getLeaveRequestById(leaveRequestId);
        if (requestOpt.isEmpty()) {
            return false;
        }

        LeaveRequest request = requestOpt.get();

        if (!"Chờ duyệt".equals(request.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể duyệt đơn đang chờ duyệt");
        }

        request.setStatus("Đã duyệt");
        request.setReviewNotes(reviewNotes);
        request.setReviewedAt(java.time.LocalDateTime.now());

        return leaveRequestDAO.update(request);
    }

    /**
     * Từ chối đơn xin nghỉ (for managers)
     */
    public boolean rejectLeaveRequest(UUID leaveRequestId, UUID reviewerId, String reviewNotes) {
        Optional<LeaveRequest> requestOpt = getLeaveRequestById(leaveRequestId);
        if (requestOpt.isEmpty()) {
            return false;
        }

        LeaveRequest request = requestOpt.get();

        if (!"Chờ duyệt".equals(request.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể từ chối đơn đang chờ duyệt");
        }

        request.setStatus("Từ chối");
        request.setReviewNotes(reviewNotes);
        request.setReviewedAt(java.time.LocalDateTime.now());

        return leaveRequestDAO.update(request);
    }

    /**
     * Đếm số ngày nghỉ đã được duyệt trong một khoảng thời gian
     */
    public long countApprovedLeaveDays(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        return leaveRequestDAO.countApprovedLeaveDays(employeeId, startDate, endDate);
    }
}
