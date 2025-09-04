package ru.practicum.ewm.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.mapper.RequestMapper;
import ru.practicum.ewm.main.model.*;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.RequestRepository;
import ru.practicum.ewm.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public RequestService(RequestRepository requestRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        // 1. Проверяем, что событие существует и принадлежит пользователю
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id=%d для пользователя с id=%d не найдено", eventId, userId)));

        return requestRepository.findByEventId(eventId)
                .stream().map(RequestMapper::mapToDto).toList();
    }

    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId,
                                                                EventRequestStatusUpdateRequest updateRequest) {
        // 1. Проверяем, что событие существует и принадлежит пользователю
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id=%d для пользователя с id=%d не найдено", eventId, userId)));

        // 2. Получаем заявки для обновления
        List<EventRequest> requestsToUpdate = requestRepository.findAllById(updateRequest.getRequestIds());

        // 3. Проверяем, что все заявки принадлежат событию
     //   validateRequestsBelongToEvent(requestsToUpdate, eventId);

        // 4. Обрабатываем в зависимости от статуса
        if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
            return confirmRequests(event, requestsToUpdate);
        } else  {
            return rejectRequests(requestsToUpdate);
        }
    }

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        // Проверяем существование пользователя
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        // Получаем все заявки пользователя
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::mapToDto).toList();
    }

    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        // 1. Проверяем существование пользователя
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        // 2. Проверяем существование события
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        // 3. Проверяем, что пользователь не является инициатором события
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на участие");
        }

        // 4. Проверяем, что событие опубликовано
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        // 5. Проверяем, что у пользователя нет активной заявки на это событие
        Optional<EventRequest> existingRequest = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (existingRequest.isPresent()) {
            throw new ConflictException("Заявка на это событие уже существует");
        }

        // 6. Проверяем лимит участников (если есть)
        if (event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Лимит участников для события исчерпан");
            }
        }

        // 7. Создаем заявку
        EventRequest request = EventRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(determineInitialStatus(event))
                .build();
        EventRequest savedRequest = requestRepository.save(request);
        return RequestMapper.mapToDto(savedRequest);
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        // 1. Проверяем существование пользователя
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        // 2. Находим заявку
        EventRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с id=" + requestId + " не найдена"));

        // 3. Проверяем, что заявка принадлежит пользователю
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Заявка не принадлежит пользователю");
        }

        // 5. Отменяем заявку
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.mapToDto(requestRepository.save(request));

    }

    private RequestStatus determineInitialStatus(Event event) {
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return RequestStatus.CONFIRMED;
        }
        return RequestStatus.PENDING;
    }

    private EventRequestStatusUpdateResult confirmRequests(Event event, List<EventRequest> requestsToConfirm) {
        // Проверяем, требуется ли подтверждение заявок
//        if (!isModerationRequired(event)) {
//            throw new ConflictException("Для данного события подтверждение заявок не требуется");
//        }

        // Проверяем, не достигнут ли лимит
        long confirmedCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long availableSlots = event.getParticipantLimit() - confirmedCount;

        if (availableSlots <= 0) {
            throw new ConflictException("Лимит заявок для события исчерпан");
        }

        if (requestsToConfirm.size() > availableSlots) {
            throw new ConflictException(
                    String.format("Недостаточно свободных мест. Доступно: %d, запрошено: %d",
                            availableSlots, requestsToConfirm.size()));
        }

        List<EventRequest> confirmedRequests = new ArrayList<>();
        List<EventRequest> rejectedRequests = new ArrayList<>();

        for (EventRequest request : requestsToConfirm) {
            // Проверяем, что заявка в состоянии ожидания
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException(
                        String.format("Заявка с id=%d не в состоянии ожидания", request.getId()));
            }

            if (confirmedRequests.size() < availableSlots) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }

        // Если лимит исчерпан, отклоняем все оставшиеся pending заявки
        if (confirmedRequests.size() == availableSlots) {
            rejectPendingRequests(event.getId());
        }

        requestRepository.saveAll(requestsToConfirm);

        return new EventRequestStatusUpdateResult(
                confirmedRequests,
                rejectedRequests
        );
    }

    private EventRequestStatusUpdateResult rejectRequests(List<EventRequest> requestsToReject) {
        for (EventRequest request : requestsToReject) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException(
                        String.format("Заявка с id=%d не в состоянии ожидания", request.getId()));
            }
            request.setStatus(RequestStatus.REJECTED);
        }

        requestRepository.saveAll(requestsToReject);

        return new EventRequestStatusUpdateResult(
                List.of(),
                requestsToReject
        );
    }

    private boolean isModerationRequired(Event event) {
        // Подтверждение не требуется если:
        // 1. Лимит участников = 0 (безлимитное событие)
        // 2. Отключена пре-модерация заявок
        return event.getParticipantLimit() != 0 && event.getRequestModeration();
    }

    private void validateRequestsBelongToEvent(List<EventRequest> requests, Long eventId) {
        for (EventRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException(
                        String.format("Заявка с id=%d не принадлежит событию с id=%d", request.getId(), eventId));
            }
        }
    }

    private void rejectPendingRequests(Long eventId) {
        List<EventRequest> pendingRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
        for (EventRequest request : pendingRequests) {
            request.setStatus(RequestStatus.REJECTED);
        }
        requestRepository.saveAll(pendingRequests);
    }
}
