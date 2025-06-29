package pl.sumatywny.travelmate.trip.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ChatNoteRequestDto {
    private UUID tripId;
}