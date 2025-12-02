package com.pcpedia.api.billing.application.service;

import com.pcpedia.api.billing.application.dto.request.CreateInvoiceRequest;
import com.pcpedia.api.billing.application.dto.response.InvoiceResponse;
import com.pcpedia.api.billing.domain.model.aggregate.Invoice;
import com.pcpedia.api.billing.domain.model.enums.InvoiceStatus;
import com.pcpedia.api.billing.domain.repository.InvoiceRepository;
import com.pcpedia.api.billing.domain.repository.PaymentRepository;
import com.pcpedia.api.iam.domain.model.aggregate.User;
import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.sales.domain.model.aggregate.Contract;
import com.pcpedia.api.sales.domain.repository.ContractRepository;
import com.pcpedia.api.shared.infrastructure.exception.BadRequestException;
import com.pcpedia.api.shared.infrastructure.exception.ForbiddenException;
import com.pcpedia.api.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public Long createInvoice(CreateInvoiceRequest dto) {
        Contract contract = contractRepository.findById(dto.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("contract.not.found")));

        // Check if contract already has an active invoice
        if (invoiceRepository.existsByContractIdAndStatusNot(dto.getContractId(), InvoiceStatus.CANCELLED)) {
            throw new BadRequestException("Este contrato ya tiene una factura activa. No se puede crear otra.");
        }

        Invoice invoice = Invoice.builder()
                .contractId(dto.getContractId())
                .userId(contract.getUserId())
                .invoiceNumber(Invoice.generateInvoiceNumber())
                .issueDate(LocalDate.now())
                .dueDate(dto.getDueDate())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .status(InvoiceStatus.PENDING)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return savedInvoice.getId();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long invoiceId, Long userId, boolean isAdmin) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("invoice.not.found")));

        if (!isAdmin && !invoice.getUserId().equals(userId)) {
            throw new ForbiddenException(getMessage("auth.access.denied"));
        }

        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable, Long userId, boolean isAdmin) {
        Page<Invoice> invoices;
        if (isAdmin) {
            invoices = invoiceRepository.findAll(pageable);
        } else {
            invoices = invoiceRepository.findByUserId(userId, pageable);
        }
        return invoices.map(this::toResponse);
    }

    public void cancelInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("invoice.not.found")));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("No se puede cancelar una factura ya pagada");
        }

        invoice.cancel();
        invoiceRepository.save(invoice);
    }

    public InvoiceResponse markAsPaid(Long invoiceId, String paymentReference) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("invoice.not.found")));

        if (!invoice.isPending()) {
            throw new BadRequestException("Solo se pueden marcar como pagadas las facturas pendientes o vencidas");
        }

        invoice.markAsPaid();
        invoiceRepository.save(invoice);
        return toResponse(invoice);
    }

    public InvoiceResponse markAsOverdue(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("invoice.not.found")));

        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new BadRequestException("Solo se pueden marcar como vencidas las facturas pendientes");
        }

        invoice.setStatus(InvoiceStatus.OVERDUE);
        invoiceRepository.save(invoice);
        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public java.util.List<InvoiceResponse> getPendingInvoices() {
        return invoiceRepository.findAllPendingInvoices()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        User user = userRepository.findById(invoice.getUserId()).orElse(null);
        Contract contract = contractRepository.findById(invoice.getContractId()).orElse(null);
        BigDecimal paidAmount = paymentRepository.sumAmountByInvoiceId(invoice.getId());

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .contractId(invoice.getContractId())
                .contractNumber(contract != null ? contract.getContractNumber() : null)
                .userId(invoice.getUserId())
                .userName(user != null ? user.getName() : null)
                .companyName(user != null ? user.getCompanyName() : null)
                .invoiceNumber(invoice.getInvoiceNumber())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .amount(invoice.getAmount())
                .paidAmount(paidAmount)
                .pendingAmount(invoice.getAmount().subtract(paidAmount))
                .status(invoice.getStatus().name())
                .description(invoice.getDescription())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
