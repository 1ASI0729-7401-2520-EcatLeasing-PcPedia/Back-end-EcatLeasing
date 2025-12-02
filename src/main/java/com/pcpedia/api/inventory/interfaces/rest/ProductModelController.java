package com.pcpedia.api.inventory.interfaces.rest;

import com.pcpedia.api.inventory.application.dto.request.CreateEquipmentBatchRequest;
import com.pcpedia.api.inventory.application.dto.request.CreateProductModelRequest;
import com.pcpedia.api.inventory.application.dto.response.ProductModelResponse;
import com.pcpedia.api.inventory.application.service.ProductModelService;
import com.pcpedia.api.shared.interfaces.rest.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-models")
@RequiredArgsConstructor
@Tag(name = "Product Models", description = "Product model management - ADMIN only")
@PreAuthorize("hasRole('ADMIN')")
public class ProductModelController {

    private final ProductModelService productModelService;
    private final MessageSource messageSource;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product model", description = "Create a new product model")
    public ResponseEntity<ApiResponse<Long>> createProductModel(@Valid @RequestBody CreateProductModelRequest request) {
        Long id = productModelService.createProductModel(request);
        String message = getMessage("productModel.created");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product model by ID", description = "Get product model details")
    public ResponseEntity<ApiResponse<ProductModelResponse>> getProductModelById(@PathVariable Long id) {
        ProductModelResponse response = productModelService.getProductModelById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List product models", description = "Get paginated list of product models")
    public ResponseEntity<ApiResponse<Page<ProductModelResponse>>> getAllProductModels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductModelResponse> models = productModelService.getAllProductModels(pageable, search, category);
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    @GetMapping("/all")
    @Operation(summary = "List all active product models", description = "Get all active product models for dropdown")
    public ResponseEntity<ApiResponse<List<ProductModelResponse>>> getAllActiveModels() {
        List<ProductModelResponse> models = productModelService.getAllActiveModels();
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product model", description = "Update product model information")
    public ResponseEntity<ApiResponse<Void>> updateProductModel(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductModelRequest request) {

        productModelService.updateProductModel(id, request);
        String message = getMessage("productModel.updated");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate product model", description = "Deactivate a product model")
    public ResponseEntity<ApiResponse<Void>> deactivateProductModel(@PathVariable Long id) {
        productModelService.deactivateProductModel(id);
        String message = getMessage("productModel.deactivated");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/batch-equipment")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create equipment batch", description = "Create multiple equipment from a product model")
    public ResponseEntity<ApiResponse<Integer>> createEquipmentBatch(@Valid @RequestBody CreateEquipmentBatchRequest request) {
        int count = productModelService.createEquipmentBatch(request);
        String message = getMessage("equipment.batchCreated");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, count));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
