package com.pcpedia.api.sales.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestRequest {

    @NotNull(message = "{validation.duration.required}")
    @Min(value = 1, message = "{validation.duration.min}")
    private Integer durationMonths;

    private String notes;

    @NotEmpty(message = "{validation.items.required}")
    @Valid
    private List<RequestItemRequest> items;
}
