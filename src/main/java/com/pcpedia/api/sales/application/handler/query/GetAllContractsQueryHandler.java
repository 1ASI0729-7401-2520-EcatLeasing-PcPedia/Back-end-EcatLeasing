package com.pcpedia.api.sales.application.handler.query;

import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.inventory.domain.repository.EquipmentRepository;
import com.pcpedia.api.sales.application.dto.response.ContractItemResponse;
import com.pcpedia.api.sales.application.dto.response.ContractResponse;
import com.pcpedia.api.sales.application.query.GetAllContractsQuery;
import com.pcpedia.api.sales.domain.model.aggregate.Contract;
import com.pcpedia.api.sales.domain.model.entity.ContractItem;
import com.pcpedia.api.sales.domain.repository.ContractRepository;
import com.pcpedia.api.shared.application.cqrs.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAllContractsQueryHandler implements QueryHandler<GetAllContractsQuery, Page<ContractResponse>> {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    public Page<ContractResponse> handle(GetAllContractsQuery query) {
        Page<Contract> contracts;

        if (query.getUserId() != null && query.getStatus() != null) {
            contracts = contractRepository.findByUserIdAndStatus(query.getUserId(), query.getStatus(), query.getPageable());
        } else if (query.getUserId() != null) {
            contracts = contractRepository.findByUserId(query.getUserId(), query.getPageable());
        } else if (query.getStatus() != null) {
            contracts = contractRepository.findByStatus(query.getStatus(), query.getPageable());
        } else {
            contracts = contractRepository.findAll(query.getPageable());
        }

        return contracts.map(this::toResponse);
    }

    private ContractResponse toResponse(Contract contract) {
        var user = userRepository.findById(contract.getUserId()).orElse(null);

        return ContractResponse.builder()
                .id(contract.getId())
                .quoteId(contract.getQuoteId())
                .userId(contract.getUserId())
                .userName(user != null ? user.getName() : null)
                .companyName(user != null ? user.getCompanyName() : null)
                .contractNumber(contract.getContractNumber())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .monthlyAmount(contract.getMonthlyAmount())
                .status(contract.getStatus().name())
                .terms(contract.getTerms())
                .items(contract.getItems().stream()
                        .map(this::toItemResponse)
                        .toList())
                .createdAt(contract.getCreatedAt())
                .build();
    }

    private ContractItemResponse toItemResponse(ContractItem item) {
        var equipment = equipmentRepository.findById(item.getEquipmentId()).orElse(null);

        return ContractItemResponse.builder()
                .id(item.getId())
                .equipmentId(item.getEquipmentId())
                .equipmentName(equipment != null ? equipment.getName() : null)
                .equipmentBrand(equipment != null ? equipment.getBrand() : null)
                .equipmentModel(equipment != null ? equipment.getModel() : null)
                .equipmentSerialNumber(equipment != null ? equipment.getSerialNumber() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
