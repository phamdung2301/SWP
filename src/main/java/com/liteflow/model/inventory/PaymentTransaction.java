package com.liteflow.model.inventory;

import com.liteflow.model.auth.User;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PaymentTransaction: Giao dịch thanh toán
 */
@Entity
@jakarta.persistence.Table(name = "PaymentTransactions")
public class PaymentTransaction implements Serializable {

    @Id
    @Column(name = "TransactionID", columnDefinition = "uniqueidentifier")
    private UUID transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SessionID", nullable = false)
    private TableSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID")
    private Order order;

    @Column(name = "Amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "PaymentMethod", length = 50, nullable = false)
    private String paymentMethod;

    @Column(name = "PaymentStatus", length = 50)
    private String paymentStatus = "Completed";

    @Column(name = "TransactionReference", length = 200)
    private String transactionReference;

    @Column(name = "VNPayTransactionNo", length = 50, nullable = true)
    private String vnpayTransactionNo;

    @Column(name = "VNPayResponseCode", length = 10, nullable = true)
    private String vnpayResponseCode;

    @Column(name = "Notes", length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProcessedBy")
    private User processedBy;

    @Column(name = "ProcessedAt")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        if (transactionId == null) {
            transactionId = UUID.randomUUID();
        }
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }

    // Helper methods
    public boolean isCompleted() {
        return "Completed".equals(paymentStatus);
    }

    public boolean isPending() {
        return "Pending".equals(paymentStatus);
    }

    public boolean isFailed() {
        return "Failed".equals(paymentStatus);
    }

    public boolean isRefunded() {
        return "Refunded".equals(paymentStatus);
    }

    public boolean isCashPayment() {
        return "Cash".equals(paymentMethod);
    }

    public boolean isCardPayment() {
        return "Card".equals(paymentMethod);
    }

    public boolean isTransferPayment() {
        return "Transfer".equals(paymentMethod);
    }

    public boolean isWalletPayment() {
        return "Wallet".equals(paymentMethod);
    }

    public boolean isVNPayPayment() {
        return "VNPay".equals(paymentMethod);
    }

    public boolean isProcessing() {
        return "Processing".equals(paymentStatus);
    }

    public boolean isCancelled() {
        return "Cancelled".equals(paymentStatus);
    }

    // Getters & Setters
    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public TableSession getSession() {
        return session;
    }

    public void setSession(TableSession session) {
        this.session = session;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getVnpayTransactionNo() {
        return vnpayTransactionNo;
    }

    public void setVnpayTransactionNo(String vnpayTransactionNo) {
        this.vnpayTransactionNo = vnpayTransactionNo;
    }

    public String getVnpayResponseCode() {
        return vnpayResponseCode;
    }

    public void setVnpayResponseCode(String vnpayResponseCode) {
        this.vnpayResponseCode = vnpayResponseCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(User processedBy) {
        this.processedBy = processedBy;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public String toString() {
        return "PaymentTransaction{" +
                "transactionId=" + transactionId +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}
