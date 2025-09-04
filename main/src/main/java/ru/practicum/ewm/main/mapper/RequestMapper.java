package ru.practicum.ewm.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.model.EventRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestMapper {
    public static ParticipationRequestDto mapToDto(EventRequest request) {
        return new ParticipationRequestDto(request.getCreated(),
                request.getEvent().getId(), request.getId(), request.getRequester().getId(),
                request.getStatus());
    }
}
