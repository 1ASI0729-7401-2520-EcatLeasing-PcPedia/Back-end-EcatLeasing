package com.pcpedia.api.billing.interfaces.rest;

import com.pcpedia.api.billing.application.dto.request.CreateInvoiceRequest;
import com.pcpedia.api.billing.application.dto.response.InvoiceResponse;
import com.pcpedia.api.billing.application.service.InvoiceService;
import com.pcpedia.api.iam.domain.model.aggregate.User;
import com.pcpedia.api.iam.domain.repository.UserRepository;
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
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice management")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create invoice", description = "Admin creates an invoice for a contract")
    public ResponseEntity<ApiResponse<Long>> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        Long invoiceId = invoiceService.createInvoice(request);
        String message = getMessage("invoice.created");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, invoiceId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID", description = "Get invoice details")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = user.isAdmin();
        InvoiceResponse response = invoiceService.getInvoiceById(id, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List invoices", description = "Get paginated list of invoices")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = user.isAdmin();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InvoiceResponse> invoices = invoiceService.getAllInvoices(pageable, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel invoice", description = "Admin cancels an invoice")
    public ResponseEntity<ApiResponse<Void>> cancelInvoice(@PathVariable Long id) {
        invoiceService.cancelInvoice(id);
        String message = getMessage("invoice.cancelled");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark invoice as paid", description = "Admin marks an invoice as paid")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markAsPaid(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        String paymentReference = body != null ? body.get("paymentReference") : null;
        InvoiceResponse response = invoiceService.markAsPaid(id, paymentReference);
        String message = getMessage("invoice.paid");
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PutMapping("/{id}/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark invoice as overdue", description = "Admin marks an invoice as overdue")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markAsOverdue(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.markAsOverdue(id);
        String message = getMessage("invoice.overdue");
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending invoices", description = "Get list of pending invoices for payment")
    public ResponseEntity<ApiResponse<java.util.List<InvoiceResponse>>> getPendingInvoices() {
        java.util.List<InvoiceResponse> invoices = invoiceService.getPendingInvoices();
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
