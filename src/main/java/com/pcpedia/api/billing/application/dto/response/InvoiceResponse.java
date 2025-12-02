package com.pcpedia.api.billing.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Long id;
    private Long contractId;
    private String contractNumber;
    private Long userId;
    private String userName;
    private String companyName;
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String status;
    private String description;
    private LocalDateTime createdAt;
}
