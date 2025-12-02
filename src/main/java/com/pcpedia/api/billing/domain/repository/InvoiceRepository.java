package com.pcpedia.api.billing.domain.repository;

import com.pcpedia.api.billing.domain.model.aggregate.Invoice;
import com.pcpedia.api.billing.domain.model.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Page<Invoice> findByUserId(Long userId, Pageable pageable);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByUserIdAndStatus(Long userId, InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByContractId(Long contractId, Pageable pageable);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT i FROM Invoice i WHERE i.status IN ('PENDING', 'OVERDUE') AND i.userId = :userId ORDER BY i.dueDate ASC")
    List<Invoice> findPendingByUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'PENDING' AND i.dueDate < :today")
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status IN ('PENDING', 'OVERDUE')")
    BigDecimal sumPendingAmount();

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.userId = :userId AND i.status IN ('PENDING', 'OVERDUE')")
    BigDecimal sumPendingAmountByUserId(@Param("userId") Long userId);

    long countByStatus(InvoiceStatus status);

    long countByUserIdAndStatus(Long userId, InvoiceStatus status);

    // Check if contract already has an active invoice (not cancelled)
    boolean existsByContractIdAndStatusNot(Long contractId, InvoiceStatus status);

    // Get pending invoices for payment selection
    @Query("SELECT i FROM Invoice i WHERE i.status IN ('PENDING', 'OVERDUE') ORDER BY i.dueDate ASC")
    List<Invoice> findAllPendingInvoices();
}
