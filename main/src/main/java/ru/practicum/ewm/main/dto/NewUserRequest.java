package ru.practicum.ewm.main.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NewUserRequest {
    @NotBlank(message = "Поле email не может быть пустым")
    @Size(min = 6, max = 254, message = "Поле email должно быть от 6 до 254 символов")
    @Email(message = "Неверный email")
    private String email;
    @NotBlank(message = "Поле name не может быть пустым")
    @Size(min = 2, max = 250, message = "Поле name должно быть от 2 до 250 символов")
    private String name;
}