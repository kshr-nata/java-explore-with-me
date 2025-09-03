package ru.practicum.ewm.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.model.Request;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestMapper {
    public static ParticipationRequestDto mapToDto(Request request) {
        return new ParticipationRequestDto(request.getCreated(),
                request.getEvent().getId(), request.getId(), request.getRequester().getId(),
                request.getStatus());
    }
}
