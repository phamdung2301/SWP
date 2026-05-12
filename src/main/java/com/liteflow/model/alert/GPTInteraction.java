package com.liteflow.model.alert;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * GPT Interaction Entity
 * Theo dõi sử dụng GPT API, chi phí và hiệu quả
 */
@Entity
@Table(name = "GPTInteractions")
public class GPTInteraction {
    
    @Id
    @Column(name = "InteractionID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID interactionID;
    
    @Column(name = "Model", nullable = false, length = 50)
    private String model; // gpt-4o-mini, gpt-4o
    
    @Column(name = "Purpose", nullable = false, length = 100)
    private String purpose;
    
    @Column(name = "SystemPrompt", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String systemPrompt;
    
    @Column(name = "UserPrompt", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String userPrompt;
    
    @Column(name = "AssistantResponse", columnDefinition = "NVARCHAR(MAX)")
    private String assistantResponse;
    
    // Token Usage
    @Column(name = "PromptTokens")
    private Integer promptTokens = 0;
    
    @Column(name = "CompletionTokens")
    private Integer completionTokens = 0;
    
    @Column(name = "TotalTokens")
    private Integer totalTokens = 0;
    
    // Cost
    @Column(name = "EstimatedCostUSD", precision = 10, scale = 6)
    private BigDecimal estimatedCostUSD = BigDecimal.ZERO;
    
    @Column(name = "EstimatedCostVND", precision = 12, scale = 2)
    private BigDecimal estimatedCostVND = BigDecimal.ZERO;
    
    // Performance
    @Column(name = "ResponseTimeMs")
    private Integer responseTimeMs;
    
    // Status
    @Column(name = "Status", length = 20)
    private String status = "SUCCESS"; // SUCCESS, FAILED, TIMEOUT, RATE_LIMITED
    
    @Column(name = "ErrorMessage", length = 500)
    private String errorMessage;
    
    // Quality Feedback
    @Column(name = "WasHelpful")
    private Boolean wasHelpful;
    
    @Column(name = "FeedbackNotes", length = 500)
    private String feedbackNotes;
    
    // Context
    @Column(name = "RelatedAlertID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID relatedAlertID;
    
    @Column(name = "RelatedObjectType", length = 50)
    private String relatedObjectType;
    
    @Column(name = "RelatedObjectID", length = 100)
    private String relatedObjectID;
    
    // Metadata
    @Column(name = "CreatedBy", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID createdBy;
    
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    
    // Constructors
    public GPTInteraction() {
        this.interactionID = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }
    
    public GPTInteraction(String model, String purpose) {
        this();
        this.model = model;
        this.purpose = purpose;
    }
    
    // Getters and Setters
    public UUID getInteractionID() {
        return interactionID;
    }
    
    public void setInteractionID(UUID interactionID) {
        this.interactionID = interactionID;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public String getSystemPrompt() {
        return systemPrompt;
    }
    
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
    
    public String getUserPrompt() {
        return userPrompt;
    }
    
    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }
    
    public String getAssistantResponse() {
        return assistantResponse;
    }
    
    public void setAssistantResponse(String assistantResponse) {
        this.assistantResponse = assistantResponse;
    }
    
    public Integer getPromptTokens() {
        return promptTokens;
    }
    
    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }
    
    public Integer getCompletionTokens() {
        return completionTokens;
    }
    
    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }
    
    public Integer getTotalTokens() {
        return totalTokens;
    }
    
    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }
    
    public BigDecimal getEstimatedCostUSD() {
        return estimatedCostUSD;
    }
    
    public void setEstimatedCostUSD(BigDecimal estimatedCostUSD) {
        this.estimatedCostUSD = estimatedCostUSD;
    }
    
    public BigDecimal getEstimatedCostVND() {
        return estimatedCostVND;
    }
    
    public void setEstimatedCostVND(BigDecimal estimatedCostVND) {
        this.estimatedCostVND = estimatedCostVND;
    }
    
    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Boolean getWasHelpful() {
        return wasHelpful;
    }
    
    public void setWasHelpful(Boolean wasHelpful) {
        this.wasHelpful = wasHelpful;
    }
    
    public String getFeedbackNotes() {
        return feedbackNotes;
    }
    
    public void setFeedbackNotes(String feedbackNotes) {
        this.feedbackNotes = feedbackNotes;
    }
    
    public UUID getRelatedAlertID() {
        return relatedAlertID;
    }
    
    public void setRelatedAlertID(UUID relatedAlertID) {
        this.relatedAlertID = relatedAlertID;
    }
    
    public String getRelatedObjectType() {
        return relatedObjectType;
    }
    
    public void setRelatedObjectType(String relatedObjectType) {
        this.relatedObjectType = relatedObjectType;
    }
    
    public String getRelatedObjectID() {
        return relatedObjectID;
    }
    
    public void setRelatedObjectID(String relatedObjectID) {
        this.relatedObjectID = relatedObjectID;
    }
    
    public UUID getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility Methods
    @PrePersist
    protected void onCreate() {
        if (interactionID == null) {
            interactionID = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    /**
     * Calculate cost based on token usage and model pricing
     * gpt-4o-mini: Input $0.150/1M, Output $0.600/1M
     */
    public void calculateCost() {
        if ("gpt-4o-mini".equals(model)) {
            // Input: $0.150 per 1M tokens
            double inputCost = (promptTokens / 1_000_000.0) * 0.150;
            // Output: $0.600 per 1M tokens
            double outputCost = (completionTokens / 1_000_000.0) * 0.600;
            
            this.estimatedCostUSD = BigDecimal.valueOf(inputCost + outputCost);
            this.estimatedCostVND = this.estimatedCostUSD.multiply(BigDecimal.valueOf(24800));
        } else if ("gpt-4o".equals(model)) {
            // Input: $2.50 per 1M tokens
            double inputCost = (promptTokens / 1_000_000.0) * 2.50;
            // Output: $10.00 per 1M tokens
            double outputCost = (completionTokens / 1_000_000.0) * 10.00;
            
            this.estimatedCostUSD = BigDecimal.valueOf(inputCost + outputCost);
            this.estimatedCostVND = this.estimatedCostUSD.multiply(BigDecimal.valueOf(24800));
        }
    }
    
    /**
     * Record success response
     */
    public void recordSuccess(String response, int promptTok, int completionTok, int responseTime) {
        this.assistantResponse = response;
        this.promptTokens = promptTok;
        this.completionTokens = completionTok;
        this.totalTokens = promptTok + completionTok;
        this.responseTimeMs = responseTime;
        this.status = "SUCCESS";
        calculateCost();
    }
    
    /**
     * Record failure
     */
    public void recordFailure(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
    }
    
    /**
     * Check if interaction was successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }
    
    /**
     * Get cost in VND formatted
     */
    public String getFormattedCostVND() {
        return String.format("%,.0f VND", estimatedCostVND);
    }
    
    /**
     * Get cost in USD formatted
     */
    public String getFormattedCostUSD() {
        return String.format("$%.6f", estimatedCostUSD);
    }
    
    @Override
    public String toString() {
        return "GPTInteraction{" +
                "interactionID=" + interactionID +
                ", model='" + model + '\'' +
                ", purpose='" + purpose + '\'' +
                ", totalTokens=" + totalTokens +
                ", costUSD=" + estimatedCostUSD +
                ", status='" + status + '\'' +
                '}';
    }
}


