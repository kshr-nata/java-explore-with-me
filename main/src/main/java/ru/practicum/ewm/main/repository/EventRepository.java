package ru.practicum.ewm.main.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.dto.EventShortDto;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findByCategoryId(Integer categoryId);

    @Query("""
    SELECT e
    FROM Compilation comp
    JOIN comp.events e
    WHERE comp.id = :compilationId
    """)
    List<Event> findEventsByCompilationId(@Param("compilationId") Integer compilationId);

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> findEventsByAdminFilters(
            @Param("users") List<Integer> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Integer> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("SELECT COUNT(r) FROM Request r " +
            "WHERE r.event.id = :eventId AND r.status = ru.practicum.ewm.main.model.RequestStatus.CONFIRMED")
    Integer countConfirmedRequestsByEventId(@Param("eventId") Integer eventId);

    List<Event> findByInitiatorId(Integer initiatorId, Pageable pageable);

    Optional<Event> findByInitiatorIdAndId(Integer initiatorId, Integer eventId);

    @Query("SELECT e FROM Event e WHERE " +
            "e.state = 'PUBLISHED' AND " +
            "(:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:paid IS NULL OR e.paid = :paid) AND " +
            "(:rangeStart IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "ORDER BY e.eventDate DESC")
    List<Event> findPublishedEventsWithFilters(
            @Param("text") String text,
            @Param("categories") List<Integer> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    Optional<Event> findByIdAndState(Integer id, EventState state);
}
