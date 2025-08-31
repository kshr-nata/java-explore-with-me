package ru.practicum.ewm.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.ewm.main.model.Request;

import java.util.List;

@Data
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<Request> confirmedRequests;
    private List<Request> rejectedRequests;
}
