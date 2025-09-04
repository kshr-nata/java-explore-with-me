package ru.practicum.ewm.main.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.model.RequestStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "Список requestIds не может быть пустым")
    List<Long> requestIds;

    @NotNull(message = "Статус не может быть null")
    RequestStatus status;
}
