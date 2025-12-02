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
public class QuoteResponse {

    private Long id;
    private Long requestId;
    private Long userId;
    private String userName;
    private String companyName;
    private String status;
    private BigDecimal totalMonthly;
    private Integer durationMonths;
    private LocalDate validUntil;
    private String terms;
    private List<QuoteItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
