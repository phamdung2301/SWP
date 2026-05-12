package com.liteflow.model.external;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ExchangeRates")
public class ExchangeRate implements Serializable {

    @Id
    @Column(name = "RateID", columnDefinition = "uniqueidentifier")
    private UUID rateId;

    @Column(name = "Currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "RateToVND", precision = 18, scale = 6, nullable = false)
    private BigDecimal rateToVnd;

    @Column(name = "RateDate", nullable = false)
    private LocalDate rateDate;

    @Column(name = "Source", length = 100)
    private String source;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (rateId == null) {
            rateId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }

    public UUID getRateId() { return rateId; }
    public void setRateId(UUID rateId) { this.rateId = rateId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getRateToVnd() { return rateToVnd; }
    public void setRateToVnd(BigDecimal rateToVnd) { this.rateToVnd = rateToVnd; }
    public LocalDate getRateDate() { return rateDate; }
    public void setRateDate(LocalDate rateDate) { this.rateDate = rateDate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}


