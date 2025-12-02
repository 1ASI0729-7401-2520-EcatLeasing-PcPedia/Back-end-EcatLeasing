package com.pcpedia.api.inventory.interfaces.rest;

import com.pcpedia.api.inventory.application.dto.response.CatalogEquipmentResponse;
import com.pcpedia.api.inventory.application.dto.response.CatalogProductModelResponse;
import com.pcpedia.api.inventory.application.service.CatalogService;
import com.pcpedia.api.shared.interfaces.rest.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Equipment catalog for clients - NO prices shown")
@PreAuthorize("hasRole('CLIENT')")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    @Operation(summary = "Browse catalog", description = "Get available equipment without prices")
    public ResponseEntity<ApiResponse<Page<CatalogEquipmentResponse>>> getAvailableEquipment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<CatalogEquipmentResponse> equipment = catalogService.getAvailableEquipment(pageable, search, category);
        return ResponseEntity.ok(ApiResponse.success(equipment));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment details", description = "Get equipment details without price")
    public ResponseEntity<ApiResponse<CatalogEquipmentResponse>> getEquipmentById(@PathVariable Long id) {
        CatalogEquipmentResponse equipment = catalogService.getEquipmentById(id);
        return ResponseEntity.ok(ApiResponse.success(equipment));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get categories", description = "Get list of available equipment categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = catalogService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    // ============ Product Models Endpoints ============

    @GetMapping("/models")
    @Operation(summary = "Browse product models", description = "Get available product models with stock count - NO prices shown")
    public ResponseEntity<ApiResponse<Page<CatalogProductModelResponse>>> getProductModels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<CatalogProductModelResponse> models = catalogService.getProductModels(pageable, search, category);
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    @GetMapping("/models/{id}")
    @Operation(summary = "Get product model details", description = "Get product model details with stock count")
    public ResponseEntity<ApiResponse<CatalogProductModelResponse>> getProductModelById(@PathVariable Long id) {
        CatalogProductModelResponse model = catalogService.getProductModelById(id);
        return ResponseEntity.ok(ApiResponse.success(model));
    }
}
