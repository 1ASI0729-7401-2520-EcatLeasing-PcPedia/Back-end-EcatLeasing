package com.pcpedia.api.sales.application.query;

import com.pcpedia.api.sales.application.dto.response.QuoteResponse;
import com.pcpedia.api.sales.domain.model.enums.QuoteStatus;
import com.pcpedia.api.shared.application.cqrs.Query;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Data
@Builder
public class GetAllQuotesQuery implements Query<Page<QuoteResponse>> {
    private Long userId;
    private QuoteStatus status;
    private Pageable pageable;
}
