package ru.practicum.ewm.stats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.repository.StatRecord;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatsService {

    private final StatsRepository statsRepository;
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public void create(EndpointHit hit) {
        StatRecord record = StatMapper.toStatRecord(hit);
        statsRepository.save(record);
    }

    public List<ViewStats> getStats(String start, String end, List<String> uris, Boolean unique) {
        LocalDateTime startDate = LocalDateTime.parse(start, DTF);
        LocalDateTime endDate = LocalDateTime.parse(end, DTF);
        if (unique) {
            return statsRepository.searchUniqueViewStatsWithUris(startDate, endDate, uris);
        } else {
            return statsRepository.searchViewStatsWithUris(startDate, endDate, uris);
        }
    }
}
