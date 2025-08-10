package ru.practicum.ewm.dto.stats;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHit {
    private String app;
    private String uri;
    private String ip;
    private LocalDateTime timestamp;
}