package mz.payments.payment_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponse {

    private UUID id;
    private String phoneNumber;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public PaymentResponse(UUID id, String phoneNumber, BigDecimal amount,
                           String status, LocalDateTime createdAt) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}