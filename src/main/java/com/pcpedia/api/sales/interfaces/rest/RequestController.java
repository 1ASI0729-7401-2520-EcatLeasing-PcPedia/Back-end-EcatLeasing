package com.pcpedia.api.sales.interfaces.rest;

import com.pcpedia.api.iam.domain.model.aggregate.User;
import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.sales.application.dto.request.CreateRequestRequest;
import com.pcpedia.api.sales.application.dto.response.RequestResponse;
import com.pcpedia.api.sales.application.service.RequestService;
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
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "Requests", description = "Leasing requests management")
public class RequestController {

    private final RequestService requestService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Create request", description = "Client creates a leasing request")
    public ResponseEntity<ApiResponse<Long>> createRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateRequestRequest request) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Long requestId = requestService.createRequest(user.getId(), request);
        String message = getMessage("request.created");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, requestId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get request by ID", description = "Get request details")
    public ResponseEntity<ApiResponse<RequestResponse>> getRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = user.isAdmin();
        RequestResponse response = requestService.getRequestById(id, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List requests", description = "Get paginated list of requests")
    public ResponseEntity<ApiResponse<Page<RequestResponse>>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = user.isAdmin();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RequestResponse> requests = requestService.getAllRequests(pageable, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject request", description = "Admin rejects a request")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(@PathVariable Long id) {
        requestService.rejectRequest(id);
        String message = getMessage("request.rejected");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
