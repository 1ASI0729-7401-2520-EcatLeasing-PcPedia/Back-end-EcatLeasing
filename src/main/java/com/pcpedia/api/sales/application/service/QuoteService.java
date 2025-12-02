package com.pcpedia.api.sales.application.service;

import com.pcpedia.api.iam.domain.model.aggregate.User;
import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.inventory.domain.model.aggregate.Equipment;
import com.pcpedia.api.inventory.domain.repository.EquipmentRepository;
import com.pcpedia.api.sales.application.dto.request.CreateQuoteRequest;
import com.pcpedia.api.sales.application.dto.request.UpdateQuoteRequest;
import com.pcpedia.api.sales.application.dto.response.QuoteItemResponse;
import com.pcpedia.api.sales.application.dto.response.QuoteResponse;
import com.pcpedia.api.sales.domain.model.aggregate.Quote;
import com.pcpedia.api.sales.domain.model.aggregate.Request;
import com.pcpedia.api.sales.domain.model.entity.QuoteItem;
import com.pcpedia.api.sales.domain.model.enums.QuoteStatus;
import com.pcpedia.api.sales.domain.repository.QuoteRepository;
import com.pcpedia.api.sales.domain.repository.RequestRepository;
import com.pcpedia.api.shared.infrastructure.exception.BadRequestException;
import com.pcpedia.api.shared.infrastructure.exception.ForbiddenException;
import com.pcpedia.api.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final MessageSource messageSource;

    public Long createQuote(CreateQuoteRequest dto) {
        Request request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("request.not.found")));

        Quote quote = Quote.builder()
                .requestId(dto.getRequestId())
                .userId(request.getUserId())
                .durationMonths(dto.getDurationMonths())
                .validUntil(dto.getValidUntil())
                .terms(dto.getTerms())
                .status(QuoteStatus.DRAFT)
                .build();

        dto.getItems().forEach(itemDto -> {
            QuoteItem item = QuoteItem.builder()
                    .equipmentId(itemDto.getEquipmentId())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .build();
            quote.addItem(item);
        });

        request.markAsQuoted();
        requestRepository.save(request);

        Quote savedQuote = quoteRepository.save(quote);
        return savedQuote.getId();
    }

    public void updateQuote(Long quoteId, UpdateQuoteRequest dto) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("quote.not.found")));

        if (!quote.isDraft()) {
            throw new BadRequestException(getMessage("quote.cannot.update"));
        }

        quote.setDurationMonths(dto.getDurationMonths());
        quote.setValidUntil(dto.getValidUntil());
        quote.setTerms(dto.getTerms());

        quote.getItems().clear();
        dto.getItems().forEach(itemDto -> {
            QuoteItem item = QuoteItem.builder()
                    .equipmentId(itemDto.getEquipmentId())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .build();
            quote.addItem(item);
        });

        quoteRepository.save(quote);
    }

    public void sendQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("quote.not.found")));
        quote.send();
        quoteRepository.save(quote);
    }

    public void acceptQuote(Long quoteId, Long userId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("quote.not.found")));

        if (!quote.getUserId().equals(userId)) {
            throw new ForbiddenException(getMessage("auth.access.denied"));
        }

        quote.accept();
        quoteRepository.save(quote);
    }

    public void rejectQuote(Long quoteId, Long userId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("quote.not.found")));

        if (!quote.getUserId().equals(userId)) {
            throw new ForbiddenException(getMessage("auth.access.denied"));
        }

        quote.reject();
        quoteRepository.save(quote);
    }

    @Transactional(readOnly = true)
    public QuoteResponse getQuoteById(Long quoteId, Long userId, boolean isAdmin) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("quote.not.found")));

        if (!isAdmin && !quote.getUserId().equals(userId)) {
            throw new ForbiddenException(getMessage("auth.access.denied"));
        }

        return toResponse(quote);
    }

    @Transactional(readOnly = true)
    public Page<QuoteResponse> getAllQuotes(Pageable pageable, Long userId, boolean isAdmin) {
        Page<Quote> quotes;
        if (isAdmin) {
            quotes = quoteRepository.findAll(pageable);
        } else {
            quotes = quoteRepository.findByUserId(userId, pageable);
        }
        return quotes.map(this::toResponse);
    }

    private QuoteResponse toResponse(Quote quote) {
        User user = userRepository.findById(quote.getUserId()).orElse(null);
        List<Long> equipmentIds = quote.getItems().stream()
                .map(QuoteItem::getEquipmentId)
                .collect(Collectors.toList());
        Map<Long, Equipment> equipmentMap = equipmentRepository.findAllById(equipmentIds)
                .stream().collect(Collectors.toMap(Equipment::getId, e -> e));

        List<QuoteItemResponse> itemResponses = quote.getItems().stream()
                .map(item -> {
                    Equipment eq = equipmentMap.get(item.getEquipmentId());
                    return QuoteItemResponse.builder()
                            .id(item.getId())
                            .equipmentId(item.getEquipmentId())
                            .equipmentName(eq != null ? eq.getName() : null)
                            .equipmentBrand(eq != null ? eq.getBrand() : null)
                            .equipmentModel(eq != null ? eq.getModel() : null)
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(item.getSubtotal())
                            .build();
                }).collect(Collectors.toList());

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
                .items(itemResponses)
                .createdAt(quote.getCreatedAt())
                .sentAt(quote.getSentAt())
                .build();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
