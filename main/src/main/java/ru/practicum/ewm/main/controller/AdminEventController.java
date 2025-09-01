package ru.practicum.ewm.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @Autowired
    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventFullDto> searchEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        return eventService.searchEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("{eventId}")
    public EventFullDto updateEventByAdmin(
            @PathVariable Long eventId,
            @RequestBody UpdateEventAdminRequest updateRequest) {

        return eventService.updateEventByAdmin(eventId, updateRequest);
    }
}
