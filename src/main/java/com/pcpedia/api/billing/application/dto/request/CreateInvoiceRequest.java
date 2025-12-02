package com.pcpedia.api.billing.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {

    @NotNull(message = "{validation.contract.required}")
    private Long contractId;

    @NotNull(message = "{validation.dueDate.required}")
    private LocalDate dueDate;

    @NotNull(message = "{validation.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.amount.min}")
    private BigDecimal amount;

    private String description;
}
