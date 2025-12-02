package com.pcpedia.api.inventory.application.service;

import com.pcpedia.api.inventory.application.command.ChangeEquipmentStatusCommand;
import com.pcpedia.api.inventory.application.command.CreateEquipmentCommand;
import com.pcpedia.api.inventory.application.command.DeleteEquipmentCommand;
import com.pcpedia.api.inventory.application.command.UpdateEquipmentCommand;
import com.pcpedia.api.inventory.application.dto.response.EquipmentResponse;
import com.pcpedia.api.inventory.application.handler.command.ChangeEquipmentStatusCommandHandler;
import com.pcpedia.api.inventory.application.handler.command.CreateEquipmentCommandHandler;
import com.pcpedia.api.inventory.application.handler.command.DeleteEquipmentCommandHandler;
import com.pcpedia.api.inventory.application.handler.command.UpdateEquipmentCommandHandler;
import com.pcpedia.api.inventory.application.handler.query.GetAllEquipmentQueryHandler;
import com.pcpedia.api.inventory.application.handler.query.GetEquipmentByIdQueryHandler;
import com.pcpedia.api.inventory.application.query.GetAllEquipmentQuery;
import com.pcpedia.api.inventory.application.query.GetEquipmentByIdQuery;
import com.pcpedia.api.inventory.domain.model.enums.EquipmentStatus;
import com.pcpedia.api.inventory.domain.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    // Command Handlers
    private final CreateEquipmentCommandHandler createEquipmentHandler;
    private final UpdateEquipmentCommandHandler updateEquipmentHandler;
    private final DeleteEquipmentCommandHandler deleteEquipmentHandler;
    private final ChangeEquipmentStatusCommandHandler changeStatusHandler;

    // Query Handlers
    private final GetEquipmentByIdQueryHandler getEquipmentByIdHandler;
    private final GetAllEquipmentQueryHandler getAllEquipmentHandler;

    private final EquipmentRepository equipmentRepository;

    // Commands
    public Long createEquipment(CreateEquipmentCommand command) {
        return createEquipmentHandler.handle(command);
    }

    public void updateEquipment(UpdateEquipmentCommand command) {
        updateEquipmentHandler.handle(command);
    }

    public void deleteEquipment(Long equipmentId) {
        deleteEquipmentHandler.handle(new DeleteEquipmentCommand(equipmentId));
    }

    public void changeEquipmentStatus(Long equipmentId, EquipmentStatus status) {
        changeStatusHandler.handle(new ChangeEquipmentStatusCommand(equipmentId, status));
    }

    // Queries
    public EquipmentResponse getEquipmentById(Long equipmentId) {
        return getEquipmentByIdHandler.handle(new GetEquipmentByIdQuery(equipmentId));
    }

    public Page<EquipmentResponse> getAllEquipment(Pageable pageable, String search, String category) {
        return getAllEquipmentHandler.handle(
                GetAllEquipmentQuery.builder()
                        .pageable(pageable)
                        .search(search)
                        .category(category)
                        .build()
        );
    }

    public List<String> getAllCategories() {
        return equipmentRepository.findAllCategories();
    }
}
