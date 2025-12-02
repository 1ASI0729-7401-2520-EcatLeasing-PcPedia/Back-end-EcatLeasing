package com.pcpedia.api.sales.application.query;

import com.pcpedia.api.sales.application.dto.response.ContractResponse;
import com.pcpedia.api.sales.domain.model.enums.ContractStatus;
import com.pcpedia.api.shared.application.cqrs.Query;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Data
@Builder
public class GetAllContractsQuery implements Query<Page<ContractResponse>> {
    private Long userId;
    private ContractStatus status;
    private Pageable pageable;
}
