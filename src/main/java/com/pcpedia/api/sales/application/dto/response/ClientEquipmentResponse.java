package com.pcpedia.api.sales.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientEquipmentResponse {

    private Long equipmentId;
    private String name;
    private String brand;
    private String model;
    private String serialNumber;
    private String category;
    private String specifications;
    private String imageUrl;
    private String contractNumber;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String contractStatus;
}
