package ru.practicum.ewm.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.ewm.main.model.EventRequest;

import java.util.List;

@Data
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<EventRequest> confirmedRequests;
    private List<EventRequest> rejectedRequests;
}
