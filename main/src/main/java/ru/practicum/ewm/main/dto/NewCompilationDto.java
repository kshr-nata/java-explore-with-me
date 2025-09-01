package ru.practicum.ewm.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NewCompilationDto {
    @NotBlank(message = "must not be blank")
    @Size(min = 1, max = 50, message = "Количество символов в поле title от 1 до 50")
    private String title;
    private List<Long> events;
    private Boolean pinned;
}
