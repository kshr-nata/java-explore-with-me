package ru.practicum.ewm.client.stats;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.dto.stats.ViewStatsRequest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class StatsClient {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${spring.application.name}")
    private String application;

    @Value("${service.stats-service.uri:http://localhost:9090}")
    private String statsServiceUri;

    private final ObjectMapper json;
    private final HttpClient httpClient;

    public StatsClient(ObjectMapper json) {
        this.json = json;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public void hit(HttpServletRequest userRequest) {
        EndpointHit hit = EndpointHit.builder()
                .app(application)
                .ip(userRequest.getRemoteAddr())
                .uri(userRequest.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(statsServiceUri + "/hit"))
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(hit)))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            log.debug("Hit recorded successfully. Status: {}", response.statusCode());
        } catch (Exception e) {
            log.error("Failed to record hit to stats service", e);
        }
    }

    public List<ViewStats> getStats(ViewStatsRequest request) {
        try {
            String queryString = buildQueryString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(statsServiceUri + "/stats" + queryString))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return json.readValue(response.body(), new TypeReference<List<ViewStats>>() {});
            }
            log.warn("Unexpected response status: {}", response.statusCode());
        } catch (Exception e) {
            log.error("Failed to get stats from stats service", e);
        }
        return Collections.emptyList();
    }

    private String buildQueryString(ViewStatsRequest request) {
        StringBuilder builder = new StringBuilder("?");

        builder.append("start=").append(encode(request.getStart().format(DTF)));
        builder.append("&end=").append(encode(request.getEnd().format(DTF)));

        if (request.getUris() != null && !request.getUris().isEmpty()) {
            builder.append("&uris=").append(String.join(",", request.getUris()));
        }

        if (request.isUnique()) {
            builder.append("&unique=true");
        }

        return builder.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}