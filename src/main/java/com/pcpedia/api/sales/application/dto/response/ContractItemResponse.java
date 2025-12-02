package com.pcpedia.api.sales.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractItemResponse {

    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentBrand;
    private String equipmentModel;
    private String equipmentSerialNumber;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
