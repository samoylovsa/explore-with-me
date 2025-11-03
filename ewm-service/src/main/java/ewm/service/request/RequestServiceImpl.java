package ewm.service.request;

import ewm.dto.event.EventFullDto;
import ewm.dto.request.UpdateStatusRequestDto_Req;
import ewm.dto.request.UpdateStatusRequestDto_Resp;
import ewm.dto.request.UserRequestDto;
import ewm.dto.user.UserDto;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import ewm.mapper.request.RequestMapper;
import ewm.model.event.EventState;
import ewm.model.request.Request;
import ewm.model.request.RequestStatus;
import ewm.repository.request.RequestRepository;
import ewm.service.event.EventService;
import ewm.service.user.UserService;
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
    private final UserService userService;
    private final EventService eventService;

    @Override
    public List<UserRequestDto> getRequestsByUser(Long userId) {
        userService.getUserById(userId);
        return requestRepository.findAllByRequesterId(userId).stream().map(requestMapper::toDto).toList();
    }

    @Override
    @Transactional
    public UserRequestDto addRequest(Long userId, Long eventId) {

        UserDto user = userService.getUserById(userId);
        EventFullDto event = eventService.getEventById(eventId);

        if (!requestRepository.findAllByEventIdAndRequesterId(eventId, userId).isEmpty())
            throw new ConflictException("Такой запрос уже существует");

        if (event.getInitiator().getId().equals(userId))
            throw new ConflictException("Инициатор не может отправить запрос на участие в своём событии");

        if (event.getState() != EventState.PUBLISHED)
            throw new ConflictException("Событие неопубликованно, нельзя обавить запрос");

        if (!eventHasFreeSlot(event)) {
            throw new ConflictException("Нет свободных мест для участия в событии с id = " + eventId);
        }

        Request request = new Request();
        request.setRequesterId(userId);
        request.setEventId(eventId);
        request.setCreated(LocalDateTime.now());
        request.setStatus(RequestStatus.PENDING);

        if (!event.getRequestModeration()) {
            //если для события отключена пре-модерация запросов на участие,
            // то запрос должен автоматически перейти в состояние подтвержденного
            request.setStatus(RequestStatus.CONFIRMED);
        }

        request = requestRepository.save(request);
        return requestMapper.toDto(request);
    }

    @Override
    public UserRequestDto cancelRequest(Long userId, Long requestId) {
        userService.getUserById(userId);
        Request request = requestMapper.toEntity(getRequestById(requestId));

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("Запрос с id = " + requestId + " не принадлежит пользователю с id = " + userId);
        }

        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        return requestMapper.toDto(request);
    }

    @Override
    public UserRequestDto getRequestById(Long requestId) {
        return requestMapper.toDto(requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id= " + requestId + " was not found")));
    }

    @Override
    public List<UserRequestDto> getRequestsByEventId(Long userId, Long eventId) {
        EventFullDto event = eventService.getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь с id = " + userId + " не является инициатором события");
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public UpdateStatusRequestDto_Resp updateRequestStatus(Long userId, Long eventId, UpdateStatusRequestDto_Req request) {
        EventFullDto event = eventService.getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь с id = " + userId + " не является инициатором события");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            //если для события лимит заявок равен 0
            // или отключена пре-модерация заявок, то подтверждение заявок не требуется
            throw new ValidationException("Для данного события подтверждение заявок не требуется");
        }

        RequestStatus newStatus = request.getStatus();
        if (newStatus != RequestStatus.CONFIRMED && newStatus != RequestStatus.REJECTED) {
            throw new ValidationException("Устанавливать можно только статусы CONFIRMED или REJECTED");
        }

        List<Request> requestsForUpdate = requestRepository.findAllById(request.getRequestIds());

        if (requestsForUpdate.size() < request.getRequestIds().size()) {
            throw new NotFoundException("Вы пытаетесь обновить запрос(ы) которых не существует");
        }

        validateRequestsForUpdate(requestsForUpdate, eventId);

        int currentConfirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int availableSlots = event.getParticipantLimit() - currentConfirmedCount;

        List<UserRequestDto> confirmedRequests = new ArrayList<>();
        List<UserRequestDto> rejectedRequests = new ArrayList<>();

        if (newStatus == RequestStatus.CONFIRMED) {
            if (availableSlots <= 0) throw new ConflictException("Свободных мест больше нет");

            int confirmedCount = 0;
            for (Request req : requestsForUpdate) {
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
                List<Request> pendingRequests = requestRepository
                        .findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);

                for (Request pendingReq : pendingRequests) {
                    pendingReq.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toDto(pendingReq));
                }

                if (!pendingRequests.isEmpty()) {
                    requestRepository.saveAll(pendingRequests);
                }
            }
        } else {
            for (Request req : requestsForUpdate) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toDto(req));
            }
        }
        requestRepository.saveAll(requestsForUpdate);
        return requestMapper.toUpdateStatusRequestDto_Resp(confirmedRequests, rejectedRequests);
    }

    private boolean eventHasFreeSlot(EventFullDto event) {
        Integer limit = event.getParticipantLimit();
        if (limit == null || limit == 0) return true;
        int confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        return confirmed < limit;
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
}
