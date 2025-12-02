package com.pcpedia.api.sales.interfaces.rest;

import com.pcpedia.api.iam.domain.model.aggregate.User;
import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.sales.application.dto.request.CreateQuoteRequest;
import com.pcpedia.api.sales.application.dto.request.UpdateQuoteRequest;
import com.pcpedia.api.sales.application.dto.response.QuoteResponse;
import com.pcpedia.api.sales.application.service.QuoteService;
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
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
@Tag(name = "Quotes", description = "Quote management")
public class QuoteController {

    private final QuoteService quoteService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create quote", description = "Admin creates a quote for a request")
    public ResponseEntity<ApiResponse<Long>> createQuote(@Valid @RequestBody CreateQuoteRequest request) {
        Long quoteId = quoteService.createQuote(request);
        String message = getMessage("quote.created");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, quoteId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quote by ID", description = "Get quote details")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuoteById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = user.isAdmin();
        QuoteResponse response = quoteService.getQuoteById(id, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List quotes", description = "Get paginated list of quotes")
    public ResponseEntity<ApiResponse<Page<QuoteResponse>>> getAllQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = user.isAdmin();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuoteResponse> quotes = quoteService.getAllQuotes(pageable, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update quote", description = "Admin updates a draft quote")
    public ResponseEntity<ApiResponse<Void>> updateQuote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuoteRequest request) {

        quoteService.updateQuote(id, request);
        String message = getMessage("quote.updated");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PatchMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send quote", description = "Admin sends quote to client")
    public ResponseEntity<ApiResponse<Void>> sendQuote(@PathVariable Long id) {
        quoteService.sendQuote(id);
        String message = getMessage("quote.sent");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Accept quote", description = "Client accepts a quote")
    public ResponseEntity<ApiResponse<Void>> acceptQuote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        quoteService.acceptQuote(id, user.getId());
        String message = getMessage("quote.accepted");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Reject quote", description = "Client rejects a quote")
    public ResponseEntity<ApiResponse<Void>> rejectQuote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        quoteService.rejectQuote(id, user.getId());
        String message = getMessage("quote.rejected");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
