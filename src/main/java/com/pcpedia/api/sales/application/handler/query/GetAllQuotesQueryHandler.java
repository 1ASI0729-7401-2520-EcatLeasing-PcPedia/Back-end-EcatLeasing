package com.pcpedia.api.sales.application.handler.query;

import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.inventory.domain.repository.EquipmentRepository;
import com.pcpedia.api.sales.application.dto.response.QuoteItemResponse;
import com.pcpedia.api.sales.application.dto.response.QuoteResponse;
import com.pcpedia.api.sales.application.query.GetAllQuotesQuery;
import com.pcpedia.api.sales.domain.model.aggregate.Quote;
import com.pcpedia.api.sales.domain.model.entity.QuoteItem;
import com.pcpedia.api.sales.domain.repository.QuoteRepository;
import com.pcpedia.api.shared.application.cqrs.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAllQuotesQueryHandler implements QueryHandler<GetAllQuotesQuery, Page<QuoteResponse>> {

    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    public Page<QuoteResponse> handle(GetAllQuotesQuery query) {
        Page<Quote> quotes;

        if (query.getUserId() != null && query.getStatus() != null) {
            quotes = quoteRepository.findByUserIdAndStatus(query.getUserId(), query.getStatus(), query.getPageable());
        } else if (query.getUserId() != null) {
            quotes = quoteRepository.findByUserId(query.getUserId(), query.getPageable());
        } else if (query.getStatus() != null) {
            quotes = quoteRepository.findByStatus(query.getStatus(), query.getPageable());
        } else {
            quotes = quoteRepository.findAll(query.getPageable());
        }

        return quotes.map(this::toResponse);
    }

    private QuoteResponse toResponse(Quote quote) {
        var user = userRepository.findById(quote.getUserId()).orElse(null);

        return QuoteResponse.builder()
                .id(quote.getId())
                .requestId(quote.getRequestId())
                .userId(quote.getUserId())
                .userName(user != null ? user.getName() : null)
                .companyName(user != null ? user.getCompanyName() : null)
                .status(quote.getStatus().name())
                .totalMonthly(quote.getTotalMonthly())
                .durationMonths(quote.getDurationMonths())
                .validUntil(quote.getValidUntil())
                .terms(quote.getTerms())
                .items(quote.getItems().stream()
                        .map(this::toItemResponse)
                        .toList())
                .createdAt(quote.getCreatedAt())
                .sentAt(quote.getSentAt())
                .build();
    }

    private QuoteItemResponse toItemResponse(QuoteItem item) {
        var equipment = equipmentRepository.findById(item.getEquipmentId()).orElse(null);

        return QuoteItemResponse.builder()
                .id(item.getId())
                .equipmentId(item.getEquipmentId())
                .equipmentName(equipment != null ? equipment.getName() : null)
                .equipmentBrand(equipment != null ? equipment.getBrand() : null)
                .equipmentModel(equipment != null ? equipment.getModel() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
