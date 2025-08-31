package ru.practicum.ewm.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.dto.EventShortDto;
import ru.practicum.ewm.main.model.Compilation;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilationMapper {

    public static CompilationDto mapToCompilationDto(Compilation compilation, List<EventShortDto> events) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setTitle(compilation.getTitle());
        dto.setPinned(compilation.getPinned());
        dto.setEvents(events);
        return dto;
    }
}