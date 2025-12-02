package com.pcpedia.api.sales.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContractRequest {

    @NotNull(message = "{validation.quote.required}")
    private Long quoteId;

    @NotNull(message = "{validation.startDate.required}")
    private LocalDate startDate;

    private String terms;
}
