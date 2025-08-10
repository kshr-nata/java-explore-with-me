package ru.practicum.ewm.dto.stats;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewStatsRequest {
    @NonNull
    String app;
    @NonNull
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime start;
    @NonNull
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime end;
    List<String> uris;
    Boolean unique;

    public Boolean isUnique() {
        return unique;
    }
}
