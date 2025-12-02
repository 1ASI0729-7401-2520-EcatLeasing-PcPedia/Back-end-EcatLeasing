package com.pcpedia.api.sales.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {

    private Long id;
    private Long quoteId;
    private Long userId;
    private String userName;
    private String companyName;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyAmount;
    private String status;
    private String terms;
    private List<ContractItemResponse> items;
    private LocalDateTime createdAt;
}
