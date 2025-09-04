package ru.practicum.ewm.main.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.dto.stats.ViewStatsRequest;
import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.dto.EventShortDto;
import ru.practicum.ewm.main.dto.UpdateCompilationRequestDto;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.mapper.CompilationMapper;
import ru.practicum.ewm.main.mapper.EventMapper;
import ru.practicum.ewm.main.model.Compilation;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.repository.CompilationRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.dto.NewCompilationDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatsClient statsClient;

    @Autowired
    public CompilationService(CompilationRepository compilationRepository, EventRepository eventRepository, StatsClient statsClient) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.statsClient = statsClient;
    }

    public List<CompilationDto> getCompilationsWithEvents(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        // 1. Получаем подборки без событий (только метаданные)
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        if (compilations.isEmpty()) {
            return List.of();
        }

        List<CompilationDto> compilationDtos = new ArrayList<>();

        for (Compilation compilation : compilations) {
            List<Event> events = eventRepository.findEventsByCompilationId(compilation.getId());

            // 3. Получаем views из статистического сервиса
            Map<Long, Long> views = getViewsForEvents(events.stream().map(Event::getId).toList());

            // 4. Заполняем transient поля
            events.forEach(event -> {
                event.setConfirmedRequests(eventRepository.countConfirmedRequestsByEventId(event.getId()));
                Long eventViews = views.get(event.getId());
                event.setViews(eventViews != null ? eventViews : 0);
            });

            // 5. Преобразуем в DTO
            List<EventShortDto> eventDtos = events.stream()
                    .map(EventMapper::mapToEventShortDto)
                    .collect(Collectors.toList());
            compilationDtos.add(CompilationMapper.mapToCompilationDto(compilation, eventDtos));
        }

        return compilationDtos;
    }

    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        List<Event> events = eventRepository.findEventsByCompilationId(compilation.getId());

        // 3. Получаем views из статистического сервиса
        Map<Long, Long> views = getViewsForEvents(events.stream().map(Event::getId).toList());

        // 4. Заполняем transient поля
        events.forEach(event -> {
            event.setConfirmedRequests(eventRepository.countConfirmedRequestsByEventId(event.getId()));
            Long eventViews = views.get(event.getId());
            event.setViews(eventViews != null ? eventViews : 0);
        });

        // 5. Преобразуем в DTO
        List<EventShortDto> eventDtos = events.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());

        return CompilationMapper.mapToCompilationDto(compilation, eventDtos);
    }

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        // 1. Создаем новую подборку
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false);

        // 2. Добавляем события в подборку, если они указаны
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(
                    newCompilationDto.getEvents().stream()
                            .map(Long::valueOf)
                            .collect(Collectors.toList())
            );
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        // 3. Сохраняем подборку
        Compilation savedCompilation = compilationRepository.save(compilation);

        List<Event> events = eventRepository.findEventsByCompilationId(compilation.getId());

        // 3. Получаем views из статистического сервиса
        Map<Long, Long> views = getViewsForEvents(events.stream().map(Event::getId).toList());

        // 4. Заполняем transient поля
        events.forEach(event -> {
            event.setConfirmedRequests(eventRepository.countConfirmedRequestsByEventId(event.getId()));
            Long eventViews = views.get(event.getId());
            event.setViews(eventViews != null ? eventViews : 0);
        });

        // 5. Преобразуем в DTO
        List<EventShortDto> eventDtos = events.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());

        // 4. Преобразуем в DTO
        return CompilationMapper.mapToCompilationDto(savedCompilation, eventDtos);
    }

    public void deleteCompilation(Long compId) {
        // 1. Проверяем существование подборки
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        // 2. Удаляем подборку
        compilationRepository.delete(compilation);

        log.info("Подборка с id={} успешно удалена", compId);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequestDto updateRequest) {
        // 1. Находим подборку
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        // 2. Обновляем поля
        compilation.setTitle(updateRequest.getTitle());
        compilation.setPinned(updateRequest.getPinned());

        if (updateRequest.getEvents() != null && !updateRequest.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(
                    updateRequest.getEvents()
            );
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        // 4. Сохраняем изменения
        Compilation updatedCompilation = compilationRepository.save(compilation);

        List<Event> events = eventRepository.findEventsByCompilationId(compilation.getId());

        // 3. Получаем views из статистического сервиса
        Map<Long, Long> views = getViewsForEvents(events.stream().map(Event::getId).toList());

        // 4. Заполняем transient поля
        events.forEach(event -> {
            event.setConfirmedRequests(eventRepository.countConfirmedRequestsByEventId(event.getId()));
            Long eventViews = views.get(event.getId());
            event.setViews(eventViews != null ? eventViews : 0);
        });

        // 5. Преобразуем в DTO
        List<EventShortDto> eventDtos = events.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());

        // 5. Возвращаем DTO
        return CompilationMapper.mapToCompilationDto(updatedCompilation, eventDtos);
    }

    private CompilationDto addStats(CompilationDto compilationDto) {
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            LocalDateTime earliestPublishedDate = compilationDto.getEvents().stream()
                    .map(EventShortDto::getPublishedOn)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);

            List<String> uris = compilationDto.getEvents().stream()
                    .map(event -> "/events/" + event.getId())
                    .collect(Collectors.toList());
            List<Long> eventIds = compilationDto.getEvents().stream()
                    .map(EventShortDto::getId)
                    .collect(Collectors.toList());
            Map<Long, Long> viewsMap = new HashMap<>();
            if (earliestPublishedDate != null) {
                viewsMap = getViewsForEvents(eventIds);
            }
            for (EventShortDto eventDto : compilationDto.getEvents()) {
                eventDto.setViews(viewsMap.getOrDefault(eventDto.getId(), 0L));
            }
        }
        return compilationDto;
    }

    private Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        // Создаем URI для событий в формате /events/{id}
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        // Запрашиваем статистику
        ViewStatsRequest statsRequest = ViewStatsRequest.builder()
                .app("ewm-main-service") // ваше название приложения
                .start(LocalDateTime.now().minusYears(1)) // за последний год
                .end(LocalDateTime.now())
                .uris(uris)
                .unique(false) // все просмотры, а не уникальные
                .build();

        List<ViewStats> stats = statsClient.getStats(statsRequest);

        // Преобразуем в мапу: eventId -> views
        return stats.stream()
                .collect(Collectors.toMap(
                        viewStats -> extractEventIdFromUri(viewStats.getUri()),
                        ViewStats::getHits
                ));
    }

    private Long extractEventIdFromUri(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }
}
