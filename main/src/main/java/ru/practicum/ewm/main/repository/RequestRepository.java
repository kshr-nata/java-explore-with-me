package ru.practicum.ewm.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.EventRequest;
import ru.practicum.ewm.main.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<EventRequest, Long> {

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<EventRequest> findByEventId(Long eventId);

    List<EventRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);

    List<EventRequest> findByRequesterId(Long requesterId);

    Optional<EventRequest> findByRequesterIdAndEventId(Long requesterId, Long eventId);
}
