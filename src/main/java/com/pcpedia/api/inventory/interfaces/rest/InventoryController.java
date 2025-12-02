package com.pcpedia.api.inventory.interfaces.rest;

import com.pcpedia.api.inventory.application.command.CreateEquipmentCommand;
import com.pcpedia.api.inventory.application.command.UpdateEquipmentCommand;
import com.pcpedia.api.inventory.application.dto.request.ChangeEquipmentStatusRequest;
import com.pcpedia.api.inventory.application.dto.request.CreateEquipmentRequest;
import com.pcpedia.api.inventory.application.dto.request.UpdateEquipmentRequest;
import com.pcpedia.api.inventory.application.dto.response.EquipmentResponse;
import com.pcpedia.api.inventory.application.mapper.EquipmentMapper;
import com.pcpedia.api.inventory.application.service.InventoryService;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Equipment inventory management - ADMIN only")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;
    private final EquipmentMapper equipmentMapper;
    private final MessageSource messageSource;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create equipment", description = "Add new equipment to inventory")
    public ResponseEntity<ApiResponse<Long>> createEquipment(@Valid @RequestBody CreateEquipmentRequest request) {
        CreateEquipmentCommand command = equipmentMapper.toCommand(request);
        Long equipmentId = inventoryService.createEquipment(command);
        String message = messageSource.getMessage(
                "equipment.created",
                null,
                "Equipment created successfully",
                LocaleContextHolder.getLocale()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, equipmentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment by ID", description = "Get equipment details with price")
    public ResponseEntity<ApiResponse<EquipmentResponse>> getEquipmentById(@PathVariable Long id) {
        EquipmentResponse equipment = inventoryService.getEquipmentById(id);
        return ResponseEntity.ok(ApiResponse.success(equipment));
    }

    @GetMapping
    @Operation(summary = "List all equipment", description = "Get paginated list of all equipment with prices")
    public ResponseEntity<ApiResponse<Page<EquipmentResponse>>> getAllEquipment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<EquipmentResponse> equipment = inventoryService.getAllEquipment(pageable, search, category);
        return ResponseEntity.ok(ApiResponse.success(equipment));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update equipment", description = "Update equipment information")
    public ResponseEntity<ApiResponse<Void>> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEquipmentRequest request) {

        UpdateEquipmentCommand command = equipmentMapper.toCommand(id, request);
        inventoryService.updateEquipment(command);
        String message = messageSource.getMessage(
                "equipment.updated",
                null,
                "Equipment updated successfully",
                LocaleContextHolder.getLocale()
        );
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete equipment", description = "Remove equipment from inventory")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable Long id) {
        inventoryService.deleteEquipment(id);
        String message = messageSource.getMessage(
                "equipment.deleted",
                null,
                "Equipment deleted successfully",
                LocaleContextHolder.getLocale()
        );
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change equipment status", description = "Update equipment status")
    public ResponseEntity<ApiResponse<Void>> changeEquipmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeEquipmentStatusRequest request) {

        inventoryService.changeEquipmentStatus(id, request.getStatus());
        String message = messageSource.getMessage(
                "equipment.status.updated",
                null,
                "Equipment status updated successfully",
                LocaleContextHolder.getLocale()
        );
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Get list of all equipment categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = inventoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
