package ru.practicum.ewm.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.Request;
import ru.practicum.ewm.main.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByEventId(Long eventId);

    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByRequesterId(Long requesterId);

    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);
}
