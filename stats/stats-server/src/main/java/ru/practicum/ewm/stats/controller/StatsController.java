package ru.practicum.ewm.stats.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;

import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class StatsController {

    private final StatsService service;

    @Autowired
    public StatsController(StatsService service) {
        this.service = service;
    }

    @PostMapping(path = "/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void hit(@RequestBody @Valid EndpointHit hit) {
        service.create(hit);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(@RequestParam @NonNull String start,
                                                    @RequestParam @NonNull String end,
                                                    @RequestParam(required = false) List<String> uris,
                                                    @RequestParam(defaultValue = "false") Boolean unique) {
        List<ViewStats> results = service.getStats(start, end, uris, unique);
        return ResponseEntity.ok(results);
    }
}
