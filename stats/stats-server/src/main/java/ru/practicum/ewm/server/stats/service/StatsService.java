package ru.practicum.ewm.server.stats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.server.stats.exception.BadRequestException;
import ru.practicum.ewm.server.stats.repository.StatRecord;
import ru.practicum.ewm.server.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatsService {

    private final StatsRepository statsRepository;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public void create(EndpointHit hit) {
        StatRecord record = StatMapper.toStatRecord(hit);
        statsRepository.save(record);
    }

    public List<ViewStats> getStats(String start, String end, List<String> uris, Boolean unique) {
        LocalDateTime startDate = LocalDateTime.parse(start, dtf);
        LocalDateTime endDate = LocalDateTime.parse(end, dtf);
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }
        if (unique) {
            return statsRepository.searchUniqueViewStatsWithUris(startDate, endDate, uris);
        } else {
            return statsRepository.searchViewStatsWithUris(startDate, endDate, uris);
        }
    }
}
