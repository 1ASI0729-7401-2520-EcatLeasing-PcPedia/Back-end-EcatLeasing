package com.pcpedia.api.billing.interfaces.rest;

import com.pcpedia.api.billing.application.dto.request.RegisterPaymentRequest;
import com.pcpedia.api.billing.application.dto.response.PaymentResponse;
import com.pcpedia.api.billing.application.service.PaymentService;
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

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management - ADMIN only")
@PreAuthorize("hasRole('ADMIN')")
public class PaymentController {

    private final PaymentService paymentService;
    private final MessageSource messageSource;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register payment", description = "Admin registers a payment for an invoice")
    public ResponseEntity<ApiResponse<Long>> registerPayment(@Valid @RequestBody RegisterPaymentRequest request) {
        Long paymentId = paymentService.registerPayment(request);
        String message = getMessage("payment.registered");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, paymentId));
    }

    @GetMapping
    @Operation(summary = "List payments", description = "Get paginated list of all payments")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        Page<PaymentResponse> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Get payment details by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
