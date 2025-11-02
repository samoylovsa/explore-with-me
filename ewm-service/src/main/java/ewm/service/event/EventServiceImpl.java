package ewm.service.event;

import ewm.dto.event.*;
import ewm.dto.request.CountConfirmedRequestsByEventId;
import ewm.exception.*;
import ewm.dto.event.EventFullDto;
import ewm.dto.event.EventShortDto;
import ewm.dto.event.NewEventDto;
import ewm.dto.event.UpdateEventUserRequest;
import ewm.exception.BusinessRuleException;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import ewm.dto.event.*;
import ewm.dto.request.CountConfirmedRequestsByEventId;
import ewm.exception.*;
import ewm.mapper.event.EventMapper;
import ewm.model.category.Category;
import ewm.model.event.Event;
import ewm.model.event.EventState;
import ewm.model.user.User;
import ewm.repository.category.CategoryRepository;
import ewm.model.user.User;
import ewm.repository.category.CategoryRepository;
import ewm.repository.event.EventRepository;
import ewm.repository.user.UserRepository;
import ewm.repository.event.SearchEventSpecifications;
import ewm.repository.request.RequestRepository;
import ewm.repository.event.SearchEventSpecifications;
import ewm.repository.request.RequestRepository;
import ewm.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User with id=%d was not found", userId)));

        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(() ->
                new NotFoundException(String.format("Category with id=%d was not found", newEventDto.getCategory())));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BusinessRuleException(String.format("Field: eventDate. Error: должно содержать дату, " +
                    "которая еще не наступила. Value:%s", newEventDto.getEventDate().toString()));
        }

        Event event = eventMapper.toEntity(newEventDto, initiator, category);
        event = eventRepository.save(event);
        return eventMapper.toFullDto(event, 0, 0);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        Page<Event> page = eventRepository.findAllByInitiatorId(userId, pageable);
        List<Long> searchEventIds = page.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);
        return page.stream().map(event -> eventMapper.toShortDto(event,
                confirmedRequestsCount.getOrDefault(event.getId(), 0),
                0)).toList();
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Event with id=%d not found", eventId)));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new BusinessRuleException(String.format("User with id=%d is not the initiator of this event", userId));
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Event must not be published");
        }

        if (updateEventUserRequest.getEventDate() != null
                && updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BusinessRuleException(String.format("Field: eventDate. Error: должно содержать дату, " +
                    "которая еще не наступила. Value:%s", updateEventUserRequest.getEventDate().toString()));
        }

        if (updateEventUserRequest.getTitle() != null)
            event.setTitle(updateEventUserRequest.getTitle());
        if (updateEventUserRequest.getDescription() != null)
            event.setDescription(updateEventUserRequest.getDescription());
        if (updateEventUserRequest.getAnnotation() != null)
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory()).orElseThrow(
                    () -> new NotFoundException(String.format("Category with id=%d not found",
                            updateEventUserRequest.getCategory()))
            );
            event.setCategory(category);
        }
        if (updateEventUserRequest.getEventDate() != null)
            event.setEventDate(updateEventUserRequest.getEventDate());
        if (updateEventUserRequest.getPaid() != null)
            event.setPaid(updateEventUserRequest.getPaid());
        if (updateEventUserRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        if (updateEventUserRequest.getRequestModeration() != null)
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        if (updateEventUserRequest.getLocation() != null) {
            event.getLocation().setLat(updateEventUserRequest.getLocation().getLat());
            event.getLocation().setLon(updateEventUserRequest.getLocation().getLon());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW:
                    if (event.getState() == EventState.PENDING) {
                        event.setState(EventState.CANCELED);
                    } else {
                        throw new BusinessRuleException("Only pending events can be canceled");
                    }
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        }
        event = eventRepository.save(event);
        List<Long> searchEventIds = List.of(eventId);
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);
        return eventMapper.toFullDto(event, confirmedRequestsCount.getOrDefault(eventId,0), 0);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Event with id=%d not found", eventId)));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException(String.format("Event with id=%d not found for user with id=%d", eventId,
                    userId));
        }

        List<Long> searchEventIds = List.of(eventId);
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);
        return eventMapper.toFullDto(event, confirmedRequestsCount.getOrDefault(eventId, 0), 0);
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id= " + eventId + " was not found"));
        List<Long> searchEventIds = List.of(eventId);
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);
        return eventMapper.toFullDto(event, confirmedRequestsCount.getOrDefault(eventId, 0), 0);
    }

    @Override
    public List<EventFullDto> getEventsAdmin(GetEventAdminRequest request, Pageable pageable) {
        validateRangeStartAndEnd(request.getRangeStart(), request.getRangeEnd());

        Specification<Event> specification = SearchEventSpecifications.addWhereNull();
        if (request.getUsers() != null && !request.getUsers().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereUsers(request.getUsers()));
        if (request.getStates() != null && !request.getStates().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereStates(request.getStates()));
        if (request.getCategories() != null && !request.getCategories().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereCategories(request.getCategories()));
        if (request.getRangeStart() != null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(request.getRangeStart()));
        if (request.getRangeEnd() != null)
            specification = specification.and(SearchEventSpecifications.addWhereEndsAfter(request.getRangeEnd()));
        if (request.getRangeStart() == null && request.getRangeEnd() == null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(LocalDateTime.now()));

        Page<Long> eventIdsPage = eventRepository.findAll(specification, pageable).map(Event::getId);
        List<Long> eventIds = eventIdsPage.getContent();

        if (eventIds.isEmpty()) return List.of();
        List<Event> events = eventRepository.findAllById(eventIds);

        if (events.isEmpty()) return List.of();

        List<Long> searchEventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);

        return events.stream()
                .map(event -> eventMapper.toFullDto(
                        event,
                        confirmedRequestsCount.getOrDefault(event.getId(), 0),
                        0
                ))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));

        if (request.getStateAction() != null) {
            StateAdminAction stateAction = request.getStateAction();
            EventState currentState = event.getState();

            if (stateAction == StateAdminAction.PUBLISH_EVENT) {
                if (currentState != EventState.PENDING)
                    throw new ConflictException("Событие можно опубликовать только если оно в состоянии ожидания публикации");
                if (event.getEventDate() != null && event.getEventDate().isBefore(LocalDateTime.now().plusHours(1)))
                    throw new ValidationException("Дата начала события не может быть ранее чем через " + 1 + " часа(ов)");
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (stateAction == StateAdminAction.REJECT_EVENT) {
                if (currentState == EventState.PUBLISHED)
                    throw new ConflictException("Событие можно отклонить пока оно не опубликовано");
                event.setState(EventState.CANCELED);
            }
        }

        if (request.getCategory() != null) {
            categoryRepository.findById(request.getCategory()).orElseThrow(() ->
                    new NotFoundException("Категория не найдена"));
        }

        Event updatedEvent = eventRepository.save(event);
        List<Long> searchEventIds = List.of(updatedEvent.getId());
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);
        return eventMapper.toFullDto(updatedEvent,
                confirmedRequestsCount.getOrDefault(updatedEvent.getId(), 0),
                0);
    }

    @Override
    public List<EventShortDto> getEventsPublic(GetEventPublicRequest request, Pageable pageable) {
        validateRangeStartAndEnd(request.getRangeStart(), request.getRangeEnd());

        Specification<Event> specification = SearchEventSpecifications.addWhereNull();
        if (request.getText() != null && !request.getText().trim().isEmpty()) {
            specification = specification.and(SearchEventSpecifications.addLikeText(request.getText()));
        }
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            specification = specification.and(SearchEventSpecifications.addWhereCategories(request.getCategories()));
        }
        if (request.getPaid() != null) {
            specification = specification.and(SearchEventSpecifications.isPaid(request.getPaid()));
        }
        LocalDateTime rangeStart = (request.getRangeStart() == null && request.getRangeEnd() == null) ?
                LocalDateTime.now() : request.getRangeStart();
        if (rangeStart != null) {
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(rangeStart));
        }

        if (request.getRangeEnd() != null) {
            specification = specification.and(SearchEventSpecifications.addWhereEndsAfter(request.getRangeEnd()));
        }

        if (request.getOnlyAvailable()) {
            specification = specification.and(SearchEventSpecifications.addWhereAvailableSlots());
        }

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        List<Long> searchEventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);

        List<EventShortDto> result = events.stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        confirmedRequestsCount.getOrDefault(event.getId(), 0),
                        0)
                )
                .toList();

        if (SortState.VIEWS.equals(request.getSort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .toList();
        } else if (SortState.EVENT_DATE.equals(request.getSort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .toList();
        }

        return result;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .filter(ev -> ev.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));
        List<Long> searchEventIds = List.of(eventId);
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);

        return eventMapper.toFullDto(event, confirmedRequestsCount.getOrDefault(eventId, 0), 0);
    }

    private void validateRangeStartAndEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd))
            throw new BusinessRuleException("Дата начала не может быть позже даты окончания");
    }

    private Map<Long, Integer> getConfirmedRequests(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();

        List<CountConfirmedRequestsByEventId> events = requestRepository.countConfirmedRequestsByEventIds(eventIds);
        Map<Long, Integer> confirmedRequests = eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0));

        events.forEach(dto -> confirmedRequests.put(dto.getEventId(), dto.getCountConfirmedRequests()));

        return confirmedRequests;
    }
}

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id= " + eventId + " was not found"));
        List<Long> searchEventIds = List.of(eventId);
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);
        return eventMapper.toFullDto(event, confirmedRequestsCount.getOrDefault(eventId, 0), 0);
    }

    @Override
    public List<EventFullDto> getEventsAdmin(GetEventAdminRequest request, Pageable pageable) {
        validateRangeStartAndEnd(request.getRangeStart(), request.getRangeEnd());

        Specification<Event> specification = SearchEventSpecifications.addWhereNull();
        if (request.getUsers() != null && !request.getUsers().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereUsers(request.getUsers()));
        if (request.getStates() != null && !request.getStates().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereStates(request.getStates()));
        if (request.getCategories() != null && !request.getCategories().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereCategories(request.getCategories()));
        if (request.getRangeStart() != null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(request.getRangeStart()));
        if (request.getRangeEnd() != null)
            specification = specification.and(SearchEventSpecifications.addWhereEndsAfter(request.getRangeEnd()));
        if (request.getRangeStart() == null && request.getRangeEnd() == null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(LocalDateTime.now()));

        Page<Long> eventIdsPage = eventRepository.findAll(specification, pageable).map(Event::getId);
        List<Long> eventIds = eventIdsPage.getContent();

        if (eventIds.isEmpty()) return List.of();
        List<Event> events = eventRepository.findAllById(eventIds);

        if (events.isEmpty()) return List.of();

        List<Long> searchEventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);

        return events.stream()
                .map(event -> eventMapper.toFullDto(
                        event,
                        confirmedRequestsCount.getOrDefault(event.getId(), 0),
                        0
                ))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));

        if (request.getStateAction() != null) {
            StateAdminAction stateAction = request.getStateAction();
            EventState currentState = event.getState();

            if (stateAction == StateAdminAction.PUBLISH_EVENT) {
                if (currentState != EventState.PENDING)
                    throw new ConflictException("Событие можно опубликовать только если оно в состоянии ожидания публикации");
                if (event.getEventDate() != null && event.getEventDate().isBefore(LocalDateTime.now().plusHours(1)))
                    throw new ValidationException("Дата начала события не может быть ранее чем через " + 1 + " часа(ов)");
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (stateAction == StateAdminAction.REJECT_EVENT) {
                if (currentState == EventState.PUBLISHED)
                    throw new ConflictException("Событие можно отклонить пока оно не опубликовано");
                event.setState(EventState.CANCELED);
            }
        }

        if (request.getCategory() != null) {
            categoryRepository.findById(request.getCategory()).orElseThrow(() ->
                    new NotFoundException("Категория не найдена"));
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        Event updatedEvent = eventRepository.save(event);
        List<Long> searchEventIds = List.of(updatedEvent.getId());
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);
        return eventMapper.toFullDto(updatedEvent,
                confirmedRequestsCount.getOrDefault(updatedEvent.getId(), 0),
                0);
    }

    @Override
    public List<EventShortDto> getEventsPublic(GetEventPublicRequest request, Pageable pageable) {
        validateRangeStartAndEnd(request.getRangeStart(), request.getRangeEnd());

        Specification<Event> specification = SearchEventSpecifications.addWhereNull();
        if (request.getText() != null && !request.getText().trim().isEmpty()) {
            specification = specification.and(SearchEventSpecifications.addLikeText(request.getText()));
        }
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            specification = specification.and(SearchEventSpecifications.addWhereCategories(request.getCategories()));
        }
        if (request.getPaid() != null) {
            specification = specification.and(SearchEventSpecifications.isPaid(request.getPaid()));
        }
        LocalDateTime rangeStart = (request.getRangeStart() == null && request.getRangeEnd() == null) ?
                LocalDateTime.now() : request.getRangeStart();
        if (rangeStart != null) {
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(rangeStart));
        }

        if (request.getRangeEnd() != null) {
            specification = specification.and(SearchEventSpecifications.addWhereEndsAfter(request.getRangeEnd()));
        }

        if (request.getOnlyAvailable()) {
            specification = specification.and(SearchEventSpecifications.addWhereAvailableSlots());
        }

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        List<Long> searchEventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);

        List<EventShortDto> result = events.stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        confirmedRequestsCount.getOrDefault(event.getId(), 0),
                        0)
                )
                .toList();

        if (SortState.VIEWS.equals(request.getSort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .toList();
        } else if (SortState.EVENT_DATE.equals(request.getSort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .toList();
        }

        return result;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .filter(ev -> ev.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));
        List<Long> searchEventIds = List.of(eventId);
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);

        return eventMapper.toFullDto(event, confirmedRequestsCount.getOrDefault(eventId, 0), 0);
    }

    private void validateRangeStartAndEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd))
            throw new BusinessRuleException("Дата начала не может быть позже даты окончания");
    }

    private Map<Long, Integer> getConfirmedRequests(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();

        List<CountConfirmedRequestsByEventId> events = requestRepository.countConfirmedRequestsByEventIds(eventIds);
        Map<Long, Integer> confirmedRequests = eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0));

        events.forEach(dto -> confirmedRequests.put(dto.getEventId(), dto.getCountConfirmedRequests()));

        return confirmedRequests;
    }
}