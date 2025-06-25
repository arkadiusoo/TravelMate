package pl.sumatywny.travelmate.participant.service;

import org.springframework.stereotype.Component;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.Participant;

@Component
public class ParticipantMapper {

    /**
     * Converts a DTO object to an entity.
     *
     * @param dto The DTO from the client
     * @return A Participant entity
     */
    public Participant toEntity(ParticipantDTO dto) {
        if (dto == null) return null;

        return Participant.builder()
                .id(dto.getId())
                .tripId(dto.getTripId())
                .userId(dto.getUserId())
                .role(dto.getRole())
                .status(dto.getStatus())
                .email(dto.getEmail())
                .build();
    }

    /**
     * Converts an entity to a DTO object.
     *
     * @param entity The entity from the database
     * @return A ParticipantDTO
     */
    public ParticipantDTO toDTO(Participant entity) {
        if (entity == null) return null;

        return ParticipantDTO.builder()
                .id(entity.getId())
                .tripId(entity.getTripId())
                .userId(entity.getUserId())
                .role(entity.getRole())
                .status(entity.getStatus())
                .email(entity.getEmail())
                .createdAt(entity.getCreatedAt())        // NEW
                .joinedAt(entity.getJoinedAt())          // NEW
                .updatedAt(entity.getUpdatedAt())        // NEW
                .build();
    }
}