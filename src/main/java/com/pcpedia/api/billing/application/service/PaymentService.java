package com.pcpedia.api.billing.application.service;

import com.pcpedia.api.billing.application.dto.request.RegisterPaymentRequest;
import com.pcpedia.api.billing.application.dto.response.PaymentResponse;
import com.pcpedia.api.billing.domain.model.aggregate.Invoice;
import com.pcpedia.api.billing.domain.model.aggregate.Payment;
import com.pcpedia.api.billing.domain.repository.InvoiceRepository;
import com.pcpedia.api.billing.domain.repository.PaymentRepository;
import com.pcpedia.api.shared.infrastructure.exception.BadRequestException;
import com.pcpedia.api.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final MessageSource messageSource;

    public Long registerPayment(RegisterPaymentRequest dto) {
        Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("invoice.not.found")));

        if (!invoice.isPending()) {
            throw new BadRequestException(getMessage("invoice.not.pending"));
        }

        Payment payment = Payment.builder()
                .invoiceId(dto.getInvoiceId())
                .amount(dto.getAmount())
                .paymentDate(dto.getPaymentDate())
                .paymentMethod(dto.getPaymentMethod())
                .reference(dto.getReference())
                .notes(dto.getNotes())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Check if invoice is fully paid
        BigDecimal totalPaid = paymentRepository.sumAmountByInvoiceId(dto.getInvoiceId());
        if (totalPaid.compareTo(invoice.getAmount()) >= 0) {
            invoice.markAsPaid();
            invoiceRepository.save(invoice);
        }

        return savedPayment.getId();
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("payment.not.found")));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByInvoice(Long invoiceId, Pageable pageable) {
        return paymentRepository.findByInvoiceId(invoiceId, pageable).map(this::toResponse);
    }

    private PaymentResponse toResponse(Payment payment) {
        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);

        return PaymentResponse.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoiceId())
                .invoiceNumber(invoice != null ? invoice.getInvoiceNumber() : null)
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .reference(payment.getReference())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
