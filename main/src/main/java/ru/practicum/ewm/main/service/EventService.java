package ru.practicum.ewm.main.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.dto.stats.ViewStatsRequest;
import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.exception.BadRequestException;
import ru.practicum.ewm.main.mapper.EventMapper;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.model.RequestStatus;
import ru.practicum.ewm.main.model.User;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.RequestRepository;
import ru.practicum.ewm.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventService {

    private final StatsClient statsClient;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;

    @Autowired
    public EventService(StatsClient statsClient, EventRepository eventRepository, UserRepository userRepository, CategoryRepository categoryRepository, RequestRepository requestRepository) {
        this.statsClient = statsClient;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;
    }

    public List<EventFullDto> searchEventsByAdmin(List<Long> users, List<EventState> states,
                                                  List<Long> categories, LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size);

        // Получаем события из репозитория
        List<Event> events = eventRepository.findEventsByAdminFilters(
                users, states, categories, rangeStart, rangeEnd, pageable);

        if (events.isEmpty()) {
            return List.of();
        }
        // Заполняем confirmedRequests для каждого события
        events.forEach(event -> {
            Long confirmedRequests = eventRepository.countConfirmedRequestsByEventId(event.getId());
            event.setConfirmedRequests(confirmedRequests != null ? confirmedRequests : 0);
        });

        // Получаем статистику просмотров
        Map<Long, Long> views = getViewsForEvents(events);

        // Обновляем views в событиях
        events.forEach(event -> {
            Long eventViews = views.get(event.getId());
            event.setViews(eventViews != null ? eventViews : 0);
        });

        // Преобразуем в DTO
        return events.stream()
                .map(EventMapper::mapToEventFullDto)
                .collect(Collectors.toList());
    }

    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (updateRequest.getEventDate() != null && event.getPublishedOn() != null) {
            if (updateRequest.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("Дата начала события должна быть не ранее чем за час от даты публикации");
            }
        }

        // Обновляем поля события
        event = EventMapper.updateEventFromAdminRequest(event, updateRequest);

        // Обрабатываем изменение статуса
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Cannot publish the event because it's not in the right state: " + event.getState());
                    }
                    event.setPublishedOn(LocalDateTime.now());
                    event.setState(EventState.PUBLISHED);
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Cannot reject the event because it's already published");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);

        // Получаем статистику просмотров
        setViewsFromStats(updatedEvent);

        // Получаем confirmed requests
        setConfirmedRequests(event);

        return EventMapper.mapToEventFullDto(updatedEvent);
    }

    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        // 1. Получаем события пользователя
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        if (events.isEmpty()) {
            return List.of();
        }

        // 3. Получаем views из статистического сервиса
        Map<Long, Long> views = getViewsForEvents(events);

        // 4. Заполняем transient поля
        events.forEach(event -> {
            event.setConfirmedRequests(eventRepository.countConfirmedRequestsByEventId(event.getId()));
            Long eventViews = views.get(event.getId());
            event.setViews(eventViews != null ? eventViews : 0);
        });

        // 5. Преобразуем в DTO
        return events.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getEventByUserAndId(Long userId, Long eventId) {
        // 1. Получаем событие пользователя
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id=%d для пользователя с id=%d не найдено", eventId, userId)));

        // 2. Используем существующие методы для получения статистики
        List<Event> events = List.of(event);

        // Получаем views через существующий метод
        Map<Long, Long> views = getViewsForEvents(events);

        // 3. Заполняем transient поля
        event.setConfirmedRequests(eventRepository.countConfirmedRequestsByEventId(event.getId()));
        Long eventViews = views.get(event.getId());
        event.setViews(eventViews != null ? eventViews : 0);

        // 4. Преобразуем в DTO
        return EventMapper.mapToEventFullDto(event);
    }

    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        // 1. Проверяем существование пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2. Проверяем дату события (не раньше чем через 2 часа)
        validateEventDate(newEventDto.getEventDate());

        // 3. Создаем событие
        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setCategory(categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")));
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(newEventDto.getEventDate());
        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());
        event.setPaid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false);
        event.setParticipantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0);
        event.setRequestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true);
        event.setState(EventState.PENDING);
        event.setTitle(newEventDto.getTitle());

        // Устанавливаем координаты (если нужно)
        event.setLat(newEventDto.getLocation().getLat());
        event.setLon(newEventDto.getLocation().getLon());

        // 4. Сохраняем событие
        Event savedEvent = eventRepository.save(event);

        // 5. Заполняем дополнительные поля
        savedEvent.setViews(0L);
        savedEvent.setConfirmedRequests(0L);

        return EventMapper.mapToEventFullDto(savedEvent);
    }

    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        // 1. Находим событие
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id=%d для пользователя с id=%d не найдено", eventId, userId)));

        // 2. Проверяем возможность изменения
        validateEventForUpdate(event, updateRequest);

        // 3. Обновляем поля
        EventMapper.updateEventFields(event, updateRequest);

        // 4. Сохраняем
        Event updatedEvent = eventRepository.save(event);

        // 5. Получаем статистику и возвращаем DTO
        List<Event> events = List.of(updatedEvent);

        Map<Long, Long> views = getViewsForEvents(events);

        event.setConfirmedRequests(eventRepository.countConfirmedRequestsByEventId(event.getId()));
        Long eventViews = views.get(event.getId());
        event.setViews(eventViews != null ? eventViews : 0);

        return EventMapper.mapToEventFullDto(event);
    }

    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, Integer from,
                                               Integer size, HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Начало должно быть до окончания");
        }

        // 1. Устанавливаем диапазон дат по умолчанию
        LocalDateTime actualRangeStart = rangeStart;
        if (actualRangeStart == null && rangeEnd == null) {
            actualRangeStart = LocalDateTime.now();
        }

        // 2. Получаем опубликованные события с базовой фильтрацией
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findPublishedEventsWithFilters(
                text, categories, paid, actualRangeStart, rangeEnd, pageable);

        if (events.isEmpty()) {
            return List.of();
        }

        saveStats(request);

        // 3. Получаем статистику
        Map<Long, Long> views = getViewsForEvents(events);

        // 4. Заполняем transient поля
        events.forEach(event -> {
            event.setConfirmedRequests(eventRepository.countConfirmedRequestsByEventId(event.getId()));
            Long eventViews = views.get(event.getId());
            event.setViews(eventViews != null ? eventViews : 0);
        });

        // 5. Фильтруем по доступности
        List<Event> filteredEvents = events;
        if (Boolean.TRUE.equals(onlyAvailable)) {
            filteredEvents = events.stream()
                    .filter(event -> event.getConfirmedRequests() < event.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        // 6. Сортируем
        if ("VIEWS".equals(sort)) {
            filteredEvents.sort(Comparator.comparingLong(Event::getViews).reversed());
        } else if ("EVENT_DATE".equals(sort)) {
            filteredEvents.sort(Comparator.comparing(Event::getEventDate));
        }

        // 7. Преобразуем в DTO
        return filteredEvents.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());
    }


    public EventFullDto getPublishedEventById(Long eventId, HttpServletRequest request) {
        // 1. Находим опубликованное событие
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено или не опубликовано"));

        saveStats(request);

        // 2. Получаем количество подтвержденных заявок
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        event.setConfirmedRequests(confirmedRequests);

        // 3. Получаем количество просмотров
        // Получаем views через существующий метод
        List<Event> events = List.of(event);
        Map<Long, Long> views = getViewsForEvents(events);
        Long eventViews = views.get(event.getId());
        event.setViews(eventViews != null ? eventViews : 0);

        // 4. Преобразуем в DTO
        return EventMapper.mapToEventFullDto(event);
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Event date must be at least 2 hours from now");
        }
    }

    private void validateEventForUpdate(Event event, UpdateEventUserRequest updateRequest) {
        // Проверка 1: можно изменять только отмененные события или события в состоянии ожидания модерации
        if (event.getState() != EventState.CANCELED && event.getState() != EventState.PENDING) {
            throw new ConflictException("Можно изменять только отмененные события или события в состоянии ожидания модерации");
        }

        // Проверка 2: дата события не может быть раньше, чем через два часа
        if (updateRequest.getEventDate() != null) {
            LocalDateTime minAllowedDate = LocalDateTime.now().plusHours(2);
            if (updateRequest.getEventDate().isBefore(minAllowedDate)) {
                throw new ConflictException("Дата события не может быть раньше, чем через два часа от текущего момента");
            }
        }

        // Проверка 3: нельзя изменить опубликованное событие
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменять опубликованные события");
        }
    }

    private void setViewsFromStats(Event event) {
        Map<Long, Long> views = getViewsForEvents(List.of(event));
        Long eventViews = views.get(event.getId());
        event.setViews(eventViews != null ? eventViews : 0);
    }

    private void saveStats(HttpServletRequest userRequest) {
        try {
            // Используем StatsClient для отправки hit
            statsClient.hit(userRequest);
            log.debug("Hit recorded successfully for URI: {}", userRequest.getRequestURI());
        } catch (Exception e) {
            log.error("Failed to record hit to stats service for URI: {}", userRequest.getRequestURI(), e);
        }
    }

    private void setConfirmedRequests(Event event) {
        Long confirmedRequests = eventRepository.countConfirmedRequestsByEventId(event.getId());
        event.setConfirmedRequests(confirmedRequests != null ? confirmedRequests : 0);
    }

    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        // Создаем URI для событий в формате /events/{id}
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime earliestPublishedDate = events.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

//        if (eventIds.isEmpty() || earliestPublishedDate == null) {
//            return new HashMap<>();
//        }

        // Запрашиваем статистику
        ViewStatsRequest statsRequest = ViewStatsRequest.builder()
                .app("ewm-main-service") // ваше название приложения
                .start(LocalDateTime.now()) // за последний год
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
