package ru.practicum.ewm.dto.stats;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
public class ViewStats {
    private String app;
    private String uri;
    private Integer hits;

    public ViewStats(String app, String uri, Integer hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }

}