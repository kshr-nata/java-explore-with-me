package ru.practicum.ewm.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.Request;
import ru.practicum.ewm.main.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Integer> {

    Integer countByEventIdAndStatus(Integer eventId, RequestStatus status);

    List<Request> findByEventId(Integer eventId);

    List<Request> findByEventIdAndStatus(Integer eventId, RequestStatus status);

    List<Request> findByRequesterId(Integer requesterId);

    Optional<Request> findByRequesterIdAndEventId(Integer requesterId, Integer eventId);
}
