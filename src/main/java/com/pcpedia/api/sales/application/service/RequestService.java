package com.pcpedia.api.sales.application.service;

import com.pcpedia.api.iam.domain.model.aggregate.User;
import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.inventory.domain.model.aggregate.ProductModel;
import com.pcpedia.api.inventory.domain.repository.ProductModelRepository;
import com.pcpedia.api.sales.application.dto.request.CreateRequestRequest;
import com.pcpedia.api.sales.application.dto.response.RequestItemResponse;
import com.pcpedia.api.sales.application.dto.response.RequestResponse;
import com.pcpedia.api.sales.domain.model.aggregate.Request;
import com.pcpedia.api.sales.domain.model.entity.RequestItem;
import com.pcpedia.api.sales.domain.model.enums.RequestStatus;
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
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ProductModelRepository productModelRepository;
    private final MessageSource messageSource;

    public Long createRequest(Long userId, CreateRequestRequest dto) {
        Request request = Request.builder()
                .userId(userId)
                .durationMonths(dto.getDurationMonths())
                .notes(dto.getNotes())
                .status(RequestStatus.PENDING)
                .build();

        dto.getItems().forEach(itemDto -> {
            if (!productModelRepository.existsById(itemDto.getProductModelId())) {
                throw new BadRequestException(getMessage("productModel.not.found"));
            }
            RequestItem item = RequestItem.builder()
                    .productModelId(itemDto.getProductModelId())
                    .quantity(itemDto.getQuantity())
                    .build();
            request.addItem(item);
        });

        Request savedRequest = requestRepository.save(request);
        return savedRequest.getId();
    }

    @Transactional(readOnly = true)
    public RequestResponse getRequestById(Long requestId, Long userId, boolean isAdmin) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("request.not.found")));

        if (!isAdmin && !request.getUserId().equals(userId)) {
            throw new ForbiddenException(getMessage("auth.access.denied"));
        }

        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public Page<RequestResponse> getAllRequests(Pageable pageable, Long userId, boolean isAdmin) {
        Page<Request> requests;
        if (isAdmin) {
            requests = requestRepository.findAll(pageable);
        } else {
            requests = requestRepository.findByUserId(userId, pageable);
        }
        return requests.map(this::toResponse);
    }

    public void rejectRequest(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("request.not.found")));

        if (!request.isPending()) {
            throw new BadRequestException(getMessage("request.cannot.reject"));
        }

        request.reject();
        requestRepository.save(request);
    }

    private RequestResponse toResponse(Request request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);
        List<Long> productModelIds = request.getItems().stream()
                .map(RequestItem::getProductModelId)
                .collect(Collectors.toList());
        Map<Long, ProductModel> productModelMap = productModelRepository.findAllById(productModelIds)
                .stream().collect(Collectors.toMap(ProductModel::getId, pm -> pm));

        List<RequestItemResponse> itemResponses = request.getItems().stream()
                .map(item -> {
                    ProductModel pm = productModelMap.get(item.getProductModelId());
                    return RequestItemResponse.builder()
                            .id(item.getId())
                            .productModelId(item.getProductModelId())
                            .productModelName(pm != null ? pm.getName() : null)
                            .productModelBrand(pm != null ? pm.getBrand() : null)
                            .productModelModel(pm != null ? pm.getModel() : null)
                            .quantity(item.getQuantity())
                            .build();
                }).collect(Collectors.toList());

        return RequestResponse.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .userName(user != null ? user.getName() : null)
                .companyName(user != null ? user.getCompanyName() : null)
                .status(request.getStatus().name())
                .durationMonths(request.getDurationMonths())
                .notes(request.getNotes())
                .items(itemResponses)
                .createdAt(request.getCreatedAt())
                .build();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
