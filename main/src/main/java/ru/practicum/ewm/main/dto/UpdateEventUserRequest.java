package ru.practicum.ewm.main.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.main.model.Location;
import ru.practicum.ewm.main.model.UserStateAction;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000, message = "Количество символов в поле name от 20 до 2000")
    private String annotation;
    @Size(min = 20, max = 7000, message = "Количество символов в поле name от 20 до 7000")
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Future(message = "Дата события должна быть в будущем")
    private LocalDateTime eventDate;
    private Long category;
    private Location location;
    private Boolean paid;
    @PositiveOrZero(message = "Лимит участников должен быть положительным числом или нулём")
    private Long participantLimit;
    private Boolean requestModeration;
    private UserStateAction stateAction;
    @Size(min = 3, max = 120, message = "Количество символов в поле title от 20 до 2000")
    private String title;
}
