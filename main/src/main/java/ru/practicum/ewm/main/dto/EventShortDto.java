package ru.practicum.ewm.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.practicum.ewm.main.model.Category;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    private Integer id;
    private String annotation;
    private Category category;
    private Integer confirmedRequests;
    @JsonIgnore
    private LocalDateTime publishedOn;
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Integer views;
}
