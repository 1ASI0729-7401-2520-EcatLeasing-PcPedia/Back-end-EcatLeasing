package com.pcpedia.api.sales.interfaces.rest;

import com.pcpedia.api.iam.domain.model.aggregate.User;
import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.sales.application.dto.request.CreateContractRequest;
import com.pcpedia.api.sales.application.dto.response.ContractResponse;
import com.pcpedia.api.sales.application.service.ContractService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Contract management")
public class ContractController {

    private final ContractService contractService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create contract", description = "Admin creates a contract from accepted quote")
    public ResponseEntity<ApiResponse<Long>> createContract(@Valid @RequestBody CreateContractRequest request) {
        Long contractId = contractService.createContract(request);
        String message = getMessage("contract.created");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, contractId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contract by ID", description = "Get contract details")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = user.isAdmin();
        ContractResponse response = contractService.getContractById(id, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List contracts", description = "Get paginated list of contracts")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = user.isAdmin();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ContractResponse> contracts = contractService.getAllContracts(pageable, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(contracts));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel contract", description = "Admin cancels an active contract")
    public ResponseEntity<ApiResponse<Void>> cancelContract(@PathVariable Long id) {
        contractService.cancelContract(id);
        String message = getMessage("contract.cancelled");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Renew contract", description = "Admin renews an active contract")
    public ResponseEntity<ApiResponse<Long>> renewContract(
            @PathVariable Long id,
            @RequestParam(defaultValue = "12") int months) {

        Long newContractId = contractService.renewContract(id, months);
        String message = getMessage("contract.renewed");
        return ResponseEntity.ok(ApiResponse.success(message, newContractId));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
