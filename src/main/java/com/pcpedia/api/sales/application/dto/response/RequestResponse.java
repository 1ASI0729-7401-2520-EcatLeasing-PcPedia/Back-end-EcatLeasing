package com.pcpedia.api.sales.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String companyName;
    private String status;
    private Integer durationMonths;
    private String notes;
    private List<RequestItemResponse> items;
    private LocalDateTime createdAt;
}
