package com.pcpedia.api.inventory.application.service;

import com.pcpedia.api.inventory.application.dto.request.CreateEquipmentBatchRequest;
import com.pcpedia.api.inventory.application.dto.request.CreateProductModelRequest;
import com.pcpedia.api.inventory.application.dto.response.ProductModelResponse;
import com.pcpedia.api.inventory.domain.model.aggregate.Equipment;
import com.pcpedia.api.inventory.domain.model.aggregate.ProductModel;
import com.pcpedia.api.inventory.domain.model.enums.EquipmentStatus;
import com.pcpedia.api.inventory.domain.repository.EquipmentRepository;
import com.pcpedia.api.inventory.domain.repository.ProductModelRepository;
import com.pcpedia.api.shared.infrastructure.exception.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductModelService {

    private final ProductModelRepository productModelRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public Long createProductModel(CreateProductModelRequest request) {
        ProductModel productModel = ProductModel.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .model(request.getModel())
                .category(request.getCategory())
                .specifications(request.getSpecifications())
                .basePrice(request.getBasePrice())
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();

        return productModelRepository.save(productModel).getId();
    }

    @Transactional
    public void updateProductModel(Long id, CreateProductModelRequest request) {
        ProductModel productModel = productModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product model not found"));

        productModel.setName(request.getName());
        productModel.setBrand(request.getBrand());
        productModel.setModel(request.getModel());
        productModel.setCategory(request.getCategory());
        productModel.setSpecifications(request.getSpecifications());
        productModel.setBasePrice(request.getBasePrice());
        productModel.setImageUrl(request.getImageUrl());

        productModelRepository.save(productModel);
    }

    public ProductModelResponse getProductModelById(Long id) {
        ProductModel productModel = productModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product model not found"));
        return mapToResponse(productModel);
    }

    public Page<ProductModelResponse> getAllProductModels(Pageable pageable, String search, String category) {
        Page<ProductModel> page;

        if (search != null && !search.isEmpty()) {
            page = productModelRepository.searchActive(search, pageable);
        } else if (category != null && !category.isEmpty()) {
            page = productModelRepository.findByCategory(category, pageable);
        } else {
            page = productModelRepository.findByIsActiveTrue(pageable);
        }

        return page.map(this::mapToResponse);
    }

    public List<ProductModelResponse> getAllActiveModels() {
        return productModelRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void deactivateProductModel(Long id) {
        ProductModel productModel = productModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product model not found"));

        // Check if there are equipment units associated with this model
        long equipmentCount = equipmentRepository.countByProductModelId(id);
        if (equipmentCount > 0) {
            throw new BadRequestException("Cannot deactivate product model with " + equipmentCount + " equipment units. Please delete or reassign the equipment first.");
        }

        productModel.setIsActive(false);
        productModelRepository.save(productModel);
    }

    @Transactional
    public int createEquipmentBatch(CreateEquipmentBatchRequest request) {
        ProductModel productModel = productModelRepository.findById(request.getProductModelId())
                .orElseThrow(() -> new EntityNotFoundException("Product model not found"));

        List<Equipment> equipments = new ArrayList<>();

        for (String serialNumber : request.getSerialNumbers()) {
            // Check if serial number already exists
            if (equipmentRepository.existsBySerialNumber(serialNumber)) {
                throw new IllegalArgumentException("Serial number already exists: " + serialNumber);
            }

            Equipment equipment = Equipment.builder()
                    .productModel(productModel)
                    .name(productModel.getName())
                    .brand(productModel.getBrand())
                    .model(productModel.getModel())
                    .category(productModel.getCategory())
                    .specifications(productModel.getSpecifications())
                    .basePrice(productModel.getBasePrice())
                    .imageUrl(productModel.getImageUrl())
                    .serialNumber(serialNumber)
                    .purchaseDate(request.getPurchaseDate())
                    .status(EquipmentStatus.AVAILABLE)
                    .build();

            equipments.add(equipment);
        }

        equipmentRepository.saveAll(equipments);
        return equipments.size();
    }

    private ProductModelResponse mapToResponse(ProductModel pm) {
        long total = equipmentRepository.countByProductModelId(pm.getId());
        long available = equipmentRepository.countByProductModelIdAndStatus(pm.getId(), EquipmentStatus.AVAILABLE);
        long leased = equipmentRepository.countByProductModelIdAndStatus(pm.getId(), EquipmentStatus.LEASED);
        long maintenance = equipmentRepository.countByProductModelIdAndStatus(pm.getId(), EquipmentStatus.MAINTENANCE);

        return ProductModelResponse.builder()
                .id(pm.getId())
                .name(pm.getName())
                .brand(pm.getBrand())
                .model(pm.getModel())
                .category(pm.getCategory())
                .specifications(pm.getSpecifications())
                .basePrice(pm.getBasePrice())
                .imageUrl(pm.getImageUrl())
                .isActive(pm.getIsActive())
                .createdAt(pm.getCreatedAt())
                .totalEquipments(total)
                .availableEquipments(available)
                .leasedEquipments(leased)
                .maintenanceEquipments(maintenance)
                .build();
    }
}
