package ru.practicum.ewm.main.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.ewm.main.model.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "Список requestIds не может быть пустым")
    List<Integer> requestIds;

    @NotNull(message = "Статус не может быть null")
    @Pattern(regexp = "CONFIRMED|REJECTED", message = "Можно устанавливать только статусы CONFIRMED или REJECTED")
    RequestStatus status;
}
