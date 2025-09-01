package ru.practicum.ewm.main.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
public class PublicCompilationController {

    private final CompilationService compilationService;

    @Autowired
    public PublicCompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("GET /compilations?pinned={}&from={}&size={}", pinned, from, size);
        return compilationService.getCompilationsWithEvents(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("GET /compilations/{}", compId);
        return compilationService.getCompilationById(compId);
    }

}
