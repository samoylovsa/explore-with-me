package ewm.service.request;

import ewm.dto.event.EventFullDto;
import ewm.dto.request.UpdateStatusRequestDtoReq;
import ewm.dto.request.UpdateStatusRequestDtoResp;
import ewm.dto.request.UserRequestDto;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import ewm.mapper.request.RequestMapper;
import ewm.model.event.EventState;
import ewm.model.request.Request;
import ewm.model.request.RequestStatus;
import ewm.model.user.User;
import ewm.repository.request.RequestRepository;
import ewm.repository.user.UserRepository;
import ewm.service.event.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final EventService eventService;

    @Override
    public List<UserRequestDto> getRequestsByUser(Long userId) {
        getUserEntity(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserRequestDto addRequest(Long userId, Long eventId) {
        getUserEntity(userId);
        EventFullDto event = eventService.getEventById(eventId);

        validateRequestCreation(userId, eventId, event);

        Request request = createRequest(userId, eventId, event);
        request = requestRepository.save(request);
        return requestMapper.toDto(request);
    }

    @Override
    public UserRequestDto cancelRequest(Long userId, Long requestId) {
        getUserEntity(userId);
        Request request = getRequestEntity(requestId);

        validateUserOwnsRequest(request, userId);

        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        return requestMapper.toDto(request);
    }

    @Override
    public UserRequestDto getRequestById(Long requestId) {
        return requestMapper.toDto(getRequestEntity(requestId));
    }

    @Override
    public List<UserRequestDto> getRequestsByEventId(Long userId, Long eventId) {
        EventFullDto event = eventService.getEventById(eventId);
        validateUserIsInitiator(event, userId);

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public UpdateStatusRequestDtoResp updateRequestStatus(Long userId, Long eventId, UpdateStatusRequestDtoReq request) {
        EventFullDto event = eventService.getEventById(eventId);
        validateUserIsInitiator(event, userId);
        validateStatusUpdateRequired(event);

        RequestStatus newStatus = request.getStatus();
        validateStatusValue(newStatus);

        List<Request> requestsForUpdate = getRequestsForUpdate(request.getRequestIds());
        validateRequestsForUpdate(requestsForUpdate, eventId);

        return processStatusUpdate(requestsForUpdate, event, newStatus);
    }

    private void validateRequestCreation(Long userId, Long eventId, EventFullDto event) {
        if (!requestRepository.findAllByEventIdAndRequesterId(eventId, userId).isEmpty()) {
            throw new ConflictException("Такой запрос уже существует");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может отправить запрос на участие в своём событии");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Событие неопубликованно, нельзя добавить запрос");
        }

        if (!eventHasFreeSlot(event)) {
            throw new ConflictException("Нет свободных мест для участия в событии с id = " + eventId);
        }
    }

    private Request createRequest(Long userId, Long eventId, EventFullDto event) {
        Request request = new Request();
        request.setRequesterId(userId);
        request.setEventId(eventId);
        request.setCreated(LocalDateTime.now());

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        return request;
    }

    private Request getRequestEntity(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не найден"));
    }

    private void validateUserOwnsRequest(Request request, Long userId) {
        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("Запрос с id = " + request.getId() + " не принадлежит пользователю с id = " + userId);
        }
    }

    private void validateUserIsInitiator(EventFullDto event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь с id = " + userId + " не является инициатором события");
        }
    }

    private void validateStatusUpdateRequired(EventFullDto event) {
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidationException("Для данного события подтверждение заявок не требуется");
        }
    }

    private void validateStatusValue(RequestStatus status) {
        if (status != RequestStatus.CONFIRMED && status != RequestStatus.REJECTED) {
            throw new ValidationException("Устанавливать можно только статусы CONFIRMED или REJECTED");
        }
    }

    private List<Request> getRequestsForUpdate(List<Long> requestIds) {
        List<Request> requests = requestRepository.findAllById(requestIds);
        if (requests.size() < requestIds.size()) {
            throw new NotFoundException("Вы пытаетесь обновить запрос(ы) которых не существует");
        }
        return requests;
    }

    private void validateRequestsForUpdate(List<Request> requests, Long eventId) {
        for (Request req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Можно изменять только запросы в статусе PENDING");
            }

            if (!req.getEventId().equals(eventId)) {
                throw new ConflictException("Запрос с id = " + req.getId() + " не относится к событию с id = " + eventId);
            }
        }
    }

    private UpdateStatusRequestDtoResp processStatusUpdate(List<Request> requests, EventFullDto event, RequestStatus newStatus) {
        List<UserRequestDto> confirmedRequests = new ArrayList<>();
        List<UserRequestDto> rejectedRequests = new ArrayList<>();

        if (newStatus == RequestStatus.CONFIRMED) {
            processConfirmedRequests(requests, event, confirmedRequests, rejectedRequests);
        } else {
            processRejectedRequests(requests, rejectedRequests);
        }

        requestRepository.saveAll(requests);
        return requestMapper.toUpdateStatusRequestDtoResp(confirmedRequests, rejectedRequests);
    }

    private void processConfirmedRequests(List<Request> requests, EventFullDto event,
                                          List<UserRequestDto> confirmedRequests, List<UserRequestDto> rejectedRequests) {
        int currentConfirmedCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        int availableSlots = event.getParticipantLimit() - currentConfirmedCount;

        if (availableSlots <= 0) {
            throw new ConflictException("Свободных мест больше нет");
        }

        int confirmedCount = 0;
        for (Request req : requests) {
            if (confirmedCount < availableSlots) {
                req.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(requestMapper.toDto(req));
                confirmedCount++;
            } else {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toDto(req));
            }
        }

        if (currentConfirmedCount + confirmedCount >= event.getParticipantLimit()) {
            rejectPendingRequests(event.getId(), rejectedRequests);
        }
    }

    private void processRejectedRequests(List<Request> requests, List<UserRequestDto> rejectedRequests) {
        for (Request req : requests) {
            req.setStatus(RequestStatus.REJECTED);
            rejectedRequests.add(requestMapper.toDto(req));
        }
    }

    private void rejectPendingRequests(Long eventId, List<UserRequestDto> rejectedRequests) {
        List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);

        for (Request pendingReq : pendingRequests) {
            pendingReq.setStatus(RequestStatus.REJECTED);
            rejectedRequests.add(requestMapper.toDto(pendingReq));
        }

        if (!pendingRequests.isEmpty()) {
            requestRepository.saveAll(pendingRequests);
        }
    }

    private boolean eventHasFreeSlot(EventFullDto event) {
        Integer limit = event.getParticipantLimit();
        if (limit == null || limit == 0) return true;
        int confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        return confirmed < limit;
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }
}