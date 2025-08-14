package ru.practicum.ewm.server.stats.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<StatRecord, Integer> {

    @Query("select new ru.practicum.ewm.dto.stats.ViewStats(sr.app, sr.uri, count(sr.id))" +
            "from StatRecord sr " +
            "WHERE sr.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR sr.uri IN :uris) " +
            "GROUP BY sr.app, sr.uri " +
            "ORDER BY COUNT(sr.id) DESC ")
    List<ViewStats> searchViewStatsWithUris(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            @Param("uris") List<String> uris);


    @Query("SELECT new ru.practicum.ewm.dto.stats.ViewStats(sr.app, sr.uri, COUNT(DISTINCT sr.ip)) " +
            "FROM StatRecord sr " +
            "WHERE sr.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR sr.uri IN :uris) " +
            "GROUP BY sr.app, sr.uri " +
            "ORDER BY COUNT(DISTINCT sr.ip) DESC ")
    List<ViewStats> searchUniqueViewStatsWithUris(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);
}
