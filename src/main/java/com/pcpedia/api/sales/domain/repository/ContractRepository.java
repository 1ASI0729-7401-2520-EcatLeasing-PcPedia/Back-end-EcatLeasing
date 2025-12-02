package com.pcpedia.api.sales.domain.repository;

import com.pcpedia.api.sales.domain.model.aggregate.Contract;
import com.pcpedia.api.sales.domain.model.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Page<Contract> findByUserId(Long userId, Pageable pageable);

    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);

    Page<Contract> findByUserIdAndStatus(Long userId, ContractStatus status, Pageable pageable);

    Optional<Contract> findByQuoteId(Long quoteId);

    Optional<Contract> findByContractNumber(String contractNumber);

    long countByStatus(ContractStatus status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, ContractStatus status);

    @Query("SELECT COALESCE(SUM(c.monthlyAmount), 0) FROM Contract c WHERE c.status = :status")
    BigDecimal sumMonthlyAmountByStatus(@Param("status") ContractStatus status);

    @Query("SELECT COALESCE(SUM(c.monthlyAmount), 0) FROM Contract c WHERE c.userId = :userId AND c.status = :status")
    BigDecimal sumMonthlyAmountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ContractStatus status);
}
