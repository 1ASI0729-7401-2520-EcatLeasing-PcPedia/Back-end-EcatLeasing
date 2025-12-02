package com.pcpedia.api.sales.application.service;

import com.pcpedia.api.iam.domain.model.aggregate.User;
import com.pcpedia.api.iam.domain.repository.UserRepository;
import com.pcpedia.api.inventory.domain.model.aggregate.Equipment;
import com.pcpedia.api.inventory.domain.repository.EquipmentRepository;
import com.pcpedia.api.sales.application.dto.request.CreateContractRequest;
import com.pcpedia.api.sales.application.dto.response.ClientEquipmentResponse;
import com.pcpedia.api.sales.application.dto.response.ContractItemResponse;
import com.pcpedia.api.sales.application.dto.response.ContractResponse;
import com.pcpedia.api.sales.domain.model.aggregate.Contract;
import com.pcpedia.api.sales.domain.model.aggregate.Quote;
import com.pcpedia.api.sales.domain.model.entity.ContractItem;
import com.pcpedia.api.sales.domain.model.entity.QuoteItem;
import com.pcpedia.api.sales.domain.model.enums.ContractStatus;
import com.pcpedia.api.sales.domain.repository.ContractRepository;
import com.pcpedia.api.sales.domain.repository.QuoteRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final MessageSource messageSource;

    public Long createContract(CreateContractRequest dto) {
        Quote quote = quoteRepository.findById(dto.getQuoteId())
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("quote.not.found")));

        if (!quote.isAccepted()) {
            throw new BadRequestException(getMessage("quote.not.accepted"));
        }

        if (contractRepository.findByQuoteId(dto.getQuoteId()).isPresent()) {
            throw new BadRequestException(getMessage("contract.already.exists"));
        }

        Contract contract = Contract.builder()
                .quoteId(dto.getQuoteId())
                .userId(quote.getUserId())
                .contractNumber(Contract.generateContractNumber())
                .startDate(dto.getStartDate())
                .endDate(dto.getStartDate().plusMonths(quote.getDurationMonths()))
                .monthlyAmount(quote.getTotalMonthly())
                .terms(dto.getTerms() != null ? dto.getTerms() : quote.getTerms())
                .status(ContractStatus.ACTIVE)
                .build();

        quote.getItems().forEach(quoteItem -> {
            ContractItem item = ContractItem.builder()
                    .equipmentId(quoteItem.getEquipmentId())
                    .quantity(quoteItem.getQuantity())
                    .unitPrice(quoteItem.getUnitPrice())
                    .build();
            contract.addItem(item);

            // Mark equipment as leased
            Equipment equipment = equipmentRepository.findById(quoteItem.getEquipmentId()).orElse(null);
            if (equipment != null && equipment.isAvailable()) {
                equipment.markAsLeased();
                equipmentRepository.save(equipment);
            }
        });

        Contract savedContract = contractRepository.save(contract);
        return savedContract.getId();
    }

    public void cancelContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("contract.not.found")));

        contract.cancel();

        // Mark equipment as available
        contract.getItems().forEach(item -> {
            Equipment equipment = equipmentRepository.findById(item.getEquipmentId()).orElse(null);
            if (equipment != null) {
                equipment.markAsAvailable();
                equipmentRepository.save(equipment);
            }
        });

        contractRepository.save(contract);
    }

    public Long renewContract(Long contractId, int additionalMonths) {
        Contract oldContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("contract.not.found")));

        if (!oldContract.isActive()) {
            throw new BadRequestException(getMessage("contract.not.active"));
        }

        oldContract.renew();
        contractRepository.save(oldContract);

        Contract newContract = Contract.builder()
                .quoteId(oldContract.getQuoteId())
                .userId(oldContract.getUserId())
                .contractNumber(Contract.generateContractNumber())
                .startDate(oldContract.getEndDate())
                .endDate(oldContract.getEndDate().plusMonths(additionalMonths))
                .monthlyAmount(oldContract.getMonthlyAmount())
                .terms(oldContract.getTerms())
                .status(ContractStatus.ACTIVE)
                .build();

        oldContract.getItems().forEach(oldItem -> {
            ContractItem item = ContractItem.builder()
                    .equipmentId(oldItem.getEquipmentId())
                    .quantity(oldItem.getQuantity())
                    .unitPrice(oldItem.getUnitPrice())
                    .build();
            newContract.addItem(item);
        });

        Contract savedContract = contractRepository.save(newContract);
        return savedContract.getId();
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractById(Long contractId, Long userId, boolean isAdmin) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("contract.not.found")));

        if (!isAdmin && !contract.getUserId().equals(userId)) {
            throw new ForbiddenException(getMessage("auth.access.denied"));
        }

        return toResponse(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getAllContracts(Pageable pageable, Long userId, boolean isAdmin) {
        Page<Contract> contracts;
        if (isAdmin) {
            contracts = contractRepository.findAll(pageable);
        } else {
            contracts = contractRepository.findByUserId(userId, pageable);
        }
        return contracts.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ClientEquipmentResponse> getClientEquipment(Long userId) {
        List<Contract> activeContracts = contractRepository.findByUserIdAndStatus(
                userId, ContractStatus.ACTIVE, Pageable.unpaged()).getContent();

        List<ClientEquipmentResponse> result = new ArrayList<>();

        for (Contract contract : activeContracts) {
            for (ContractItem item : contract.getItems()) {
                Equipment equipment = equipmentRepository.findById(item.getEquipmentId()).orElse(null);
                if (equipment != null) {
                    result.add(ClientEquipmentResponse.builder()
                            .equipmentId(equipment.getId())
                            .name(equipment.getName())
                            .brand(equipment.getBrand())
                            .model(equipment.getModel())
                            .serialNumber(equipment.getSerialNumber())
                            .category(equipment.getCategory())
                            .specifications(equipment.getSpecifications())
                            .imageUrl(equipment.getImageUrl())
                            .contractNumber(contract.getContractNumber())
                            .contractStartDate(contract.getStartDate())
                            .contractEndDate(contract.getEndDate())
                            .contractStatus(contract.getStatus().name())
                            .build());
                }
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ClientEquipmentResponse getClientEquipmentById(Long userId, Long equipmentId) {
        List<Contract> activeContracts = contractRepository.findByUserIdAndStatus(
                userId, ContractStatus.ACTIVE, Pageable.unpaged()).getContent();

        for (Contract contract : activeContracts) {
            for (ContractItem item : contract.getItems()) {
                if (item.getEquipmentId().equals(equipmentId)) {
                    Equipment equipment = equipmentRepository.findById(equipmentId)
                            .orElseThrow(() -> new ResourceNotFoundException(getMessage("equipment.not.found")));

                    return ClientEquipmentResponse.builder()
                            .equipmentId(equipment.getId())
                            .name(equipment.getName())
                            .brand(equipment.getBrand())
                            .model(equipment.getModel())
                            .serialNumber(equipment.getSerialNumber())
                            .category(equipment.getCategory())
                            .specifications(equipment.getSpecifications())
                            .imageUrl(equipment.getImageUrl())
                            .contractNumber(contract.getContractNumber())
                            .contractStartDate(contract.getStartDate())
                            .contractEndDate(contract.getEndDate())
                            .contractStatus(contract.getStatus().name())
                            .build();
                }
            }
        }

        throw new ResourceNotFoundException(getMessage("equipment.not.found"));
    }

    private ContractResponse toResponse(Contract contract) {
        User user = userRepository.findById(contract.getUserId()).orElse(null);
        List<Long> equipmentIds = contract.getItems().stream()
                .map(ContractItem::getEquipmentId)
                .collect(Collectors.toList());
        Map<Long, Equipment> equipmentMap = equipmentRepository.findAllById(equipmentIds)
                .stream().collect(Collectors.toMap(Equipment::getId, e -> e));

        List<ContractItemResponse> itemResponses = contract.getItems().stream()
                .map(item -> {
                    Equipment eq = equipmentMap.get(item.getEquipmentId());
                    return ContractItemResponse.builder()
                            .id(item.getId())
                            .equipmentId(item.getEquipmentId())
                            .equipmentName(eq != null ? eq.getName() : null)
                            .equipmentBrand(eq != null ? eq.getBrand() : null)
                            .equipmentModel(eq != null ? eq.getModel() : null)
                            .equipmentSerialNumber(eq != null ? eq.getSerialNumber() : null)
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(item.getSubtotal())
                            .build();
                }).collect(Collectors.toList());

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
                .items(itemResponses)
                .createdAt(contract.getCreatedAt())
                .build();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
