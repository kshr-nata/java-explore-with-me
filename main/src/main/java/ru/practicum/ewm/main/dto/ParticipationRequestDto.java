package ru.practicum.ewm.main.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.model.RequestStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    LocalDateTime created;
    Long event;
    Long id;
    Long requester;
    RequestStatus status;
}
