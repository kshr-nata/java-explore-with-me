package ru.practicum.ewm.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NewCategoryDto {
    @NotBlank(message = "must not be blank")
    @Size(min = 1, max = 50, message = "Количество символов в поле name от 1 до 50")
    private String name;
}
