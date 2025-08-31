package ru.practicum.ewm.main.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.model.Request;
import ru.practicum.ewm.main.service.EventService;
import ru.practicum.ewm.main.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;
    private final RequestService requestService;

    public PrivateEventController(EventService eventService, RequestService requestService) {
        this.eventService = eventService;
        this.requestService = requestService;
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(
            @PathVariable int userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(
            @PathVariable int userId,
            @Valid @RequestBody NewEventDto newEventDto) {

        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUserAndId(
            @PathVariable int userId,
            @PathVariable int eventId) {

        log.info("GET /users/{}/events/{}", userId, eventId);
        return eventService.getEventByUserAndId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(
            @PathVariable int userId,
            @PathVariable int eventId,
            @Valid @RequestBody UpdateEventUserRequest updateRequest) {

        log.info("PATCH /users/{}/events/{} with body: {}", userId, eventId, updateRequest);
        return eventService.updateEventByUser(userId, eventId, updateRequest);
    }

    @GetMapping("{eventId}/requests")
    public List<Request> getEventRequests(
            @PathVariable int userId,
            @PathVariable int eventId) {

        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable int userId,
            @PathVariable int eventId,
            @RequestBody EventRequestStatusUpdateRequest updateRequest) {

        log.info("PATCH /users/{}/events/{}/requests with body: {}", userId, eventId, updateRequest);
        return requestService.updateRequestStatuses(userId, eventId, updateRequest);
    }


}
