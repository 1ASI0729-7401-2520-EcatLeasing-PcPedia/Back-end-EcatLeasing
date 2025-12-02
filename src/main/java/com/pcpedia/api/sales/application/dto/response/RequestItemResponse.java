package com.pcpedia.api.sales.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestItemResponse {

    private Long id;
    private Long productModelId;
    private String productModelName;
    private String productModelBrand;
    private String productModelModel;
    private Integer quantity;
}
