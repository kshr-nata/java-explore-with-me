package ru.practicum.ewm.client.stats;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.dto.stats.ViewStatsRequest;

import java.util.List;

public interface StatsClient {

    void hit(HttpServletRequest userRequest);

    List<ViewStats> getStats(ViewStatsRequest request);
}
