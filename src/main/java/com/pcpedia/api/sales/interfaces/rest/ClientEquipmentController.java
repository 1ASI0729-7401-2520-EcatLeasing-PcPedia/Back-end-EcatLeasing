package com.pcpedia.api.sales.interfaces.rest;

import com.pcpedia.api.iam.domain.model.aggregate.User;
import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.sales.application.dto.response.ClientEquipmentResponse;
import com.pcpedia.api.sales.application.service.ContractService;
import com.pcpedia.api.shared.interfaces.rest.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-equipment")
@RequiredArgsConstructor
@Tag(name = "My Equipment", description = "Client's leased equipment")
@PreAuthorize("hasRole('CLIENT')")
public class ClientEquipmentController {

    private final ContractService contractService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List my equipment", description = "Get all equipment leased by the client")
    public ResponseEntity<ApiResponse<List<ClientEquipmentResponse>>> getMyEquipment(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<ClientEquipmentResponse> equipment = contractService.getClientEquipment(user.getId());
        return ResponseEntity.ok(ApiResponse.success(equipment));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment detail", description = "Get details of a specific leased equipment")
    public ResponseEntity<ApiResponse<ClientEquipmentResponse>> getEquipmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        ClientEquipmentResponse equipment = contractService.getClientEquipmentById(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(equipment));
    }
}
