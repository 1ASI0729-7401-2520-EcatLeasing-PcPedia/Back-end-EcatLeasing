package com.pcpedia.api.sales.domain.repository;

import com.pcpedia.api.sales.domain.model.aggregate.Request;
import com.pcpedia.api.sales.domain.model.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    Page<Request> findByUserId(Long userId, Pageable pageable);

    Page<Request> findByStatus(RequestStatus status, Pageable pageable);

    Page<Request> findByUserIdAndStatus(Long userId, RequestStatus status, Pageable pageable);

    long countByStatus(RequestStatus status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, RequestStatus status);
}
