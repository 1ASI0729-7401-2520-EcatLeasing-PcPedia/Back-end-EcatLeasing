package com.pcpedia.api.sales.domain.repository;

import com.pcpedia.api.sales.domain.model.aggregate.Quote;
import com.pcpedia.api.sales.domain.model.enums.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Page<Quote> findByUserId(Long userId, Pageable pageable);

    Page<Quote> findByStatus(QuoteStatus status, Pageable pageable);

    Page<Quote> findByUserIdAndStatus(Long userId, QuoteStatus status, Pageable pageable);

    Optional<Quote> findByRequestId(Long requestId);

    long countByStatus(QuoteStatus status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, QuoteStatus status);
}
