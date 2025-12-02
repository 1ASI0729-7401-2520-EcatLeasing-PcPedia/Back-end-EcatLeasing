package com.pcpedia.api.sales.domain.model.aggregate;

import com.pcpedia.api.sales.domain.model.entity.RequestItem;
import com.pcpedia.api.sales.domain.model.enums.RequestStatus;
import com.pcpedia.api.shared.domain.model.AggregateRoot;
import com.pcpedia.api.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request extends AuditableEntity implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequestItem> items = new ArrayList<>();

    // Domain methods
    public void addItem(RequestItem item) {
        items.add(item);
        item.setRequest(this);
    }

    public void markAsQuoted() {
        this.status = RequestStatus.QUOTED;
    }

    public void reject() {
        this.status = RequestStatus.REJECTED;
    }

    public boolean isPending() {
        return this.status == RequestStatus.PENDING;
    }
}
