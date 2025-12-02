package com.pcpedia.api.inventory.application.service;

import com.pcpedia.api.inventory.application.dto.response.CatalogEquipmentResponse;
import com.pcpedia.api.inventory.application.dto.response.CatalogProductModelResponse;
import com.pcpedia.api.inventory.application.handler.query.GetAvailableEquipmentQueryHandler;
import com.pcpedia.api.inventory.application.mapper.EquipmentMapper;
import com.pcpedia.api.inventory.application.query.GetAvailableEquipmentQuery;
import com.pcpedia.api.inventory.domain.model.aggregate.Equipment;
import com.pcpedia.api.inventory.domain.model.aggregate.ProductModel;
import com.pcpedia.api.inventory.domain.model.enums.EquipmentStatus;
import com.pcpedia.api.inventory.domain.repository.EquipmentRepository;
import com.pcpedia.api.inventory.domain.repository.ProductModelRepository;
import com.pcpedia.api.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final GetAvailableEquipmentQueryHandler getAvailableEquipmentHandler;
    private final EquipmentRepository equipmentRepository;
    private final ProductModelRepository productModelRepository;
    private final EquipmentMapper equipmentMapper;
    private final MessageSource messageSource;

    public Page<CatalogEquipmentResponse> getAvailableEquipment(Pageable pageable, String search, String category) {
        return getAvailableEquipmentHandler.handle(
                GetAvailableEquipmentQuery.builder()
                        .pageable(pageable)
                        .search(search)
                        .category(category)
                        .build()
        );
    }

    public CatalogEquipmentResponse getEquipmentById(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage(
                            "equipment.not.found",
                            null,
                            "Equipment not found",
                            LocaleContextHolder.getLocale()
                    );
                    return new ResourceNotFoundException(message);
                });

        if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
            String message = messageSource.getMessage(
                    "equipment.not.available",
                    null,
                    "Equipment is not available",
                    LocaleContextHolder.getLocale()
            );
            throw new ResourceNotFoundException(message);
        }

        return equipmentMapper.toCatalogResponse(equipment);
    }

    public List<String> getCategories() {
        return equipmentRepository.findAllCategories();
    }

    /**
     * Get product models for catalog with available stock count
     * Only shows models that have at least one available equipment
     */
    public Page<CatalogProductModelResponse> getProductModels(Pageable pageable, String search, String category) {
        Page<ProductModel> page;

        if (search != null && !search.isEmpty()) {
            page = productModelRepository.searchActiveWithStock(search, pageable);
        } else if (category != null && !category.isEmpty()) {
            page = productModelRepository.findByCategoryWithStock(category, pageable);
        } else {
            page = productModelRepository.findActiveWithStock(pageable);
        }

        return page.map(this::mapToCatalogResponse);
    }

    public CatalogProductModelResponse getProductModelById(Long id) {
        ProductModel productModel = productModelRepository.findById(id)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage(
                            "productModel.not.found",
                            null,
                            "Product model not found",
                            LocaleContextHolder.getLocale()
                    );
                    return new ResourceNotFoundException(message);
                });

        return mapToCatalogResponse(productModel);
    }

    private CatalogProductModelResponse mapToCatalogResponse(ProductModel pm) {
        long available = equipmentRepository.countByProductModelIdAndStatus(pm.getId(), EquipmentStatus.AVAILABLE);

        return CatalogProductModelResponse.builder()
                .id(pm.getId())
                .name(pm.getName())
                .brand(pm.getBrand())
                .model(pm.getModel())
                .category(pm.getCategory())
                .specifications(pm.getSpecifications())
                .imageUrl(pm.getImageUrl())
                .availableStock(available)
                .build();
    }
}
