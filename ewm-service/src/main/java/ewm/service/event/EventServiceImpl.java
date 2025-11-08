package ewm.service.event;

import dto.GetStatsDto;
import dto.SaveHitDto;
import ewm.dto.event.*;
import ewm.dto.request.CountConfirmedRequestsByEventId;
import ewm.exception.BusinessRuleException;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import ewm.mapper.event.EventMapper;
import ewm.model.category.Category;
import ewm.model.event.Event;
import ewm.model.event.EventState;
import ewm.model.user.User;
import ewm.repository.category.CategoryRepository;
import ewm.repository.event.EventRepository;
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
import stats.client.StatsClient;

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
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = getUserEntity(userId);
        Category category = getCategoryEntity(newEventDto.getCategory());

        validateEventDate(newEventDto.getEventDate());

        Event event = eventMapper.toEntity(newEventDto, initiator, category);
        event = eventRepository.save(event);
        return eventMapper.toFullDto(event, 0, 0);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        Page<Event> page = eventRepository.findAllByInitiatorId(userId, pageable);

        List<Event> events = page.getContent();
        if (events.isEmpty()) return List.of();

        return getEventsShortDtoWithStats(events);
    }

    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = getEventEntity(eventId);

        validateUserIsInitiator(event, userId);
        validateEventNotPublished(event);

        if (updateEventUserRequest.getEventDate() != null) {
            validateEventDate(updateEventUserRequest.getEventDate());
        }

        updateEventFields(event, updateEventUserRequest);

        Event updatedEvent = eventRepository.save(event);
        return getEventFullDtoWithStats(updatedEvent);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId, String ip) {
        Event event = getEventEntity(eventId);
        validateUserIsInitiator(event, userId);

        EventFullDto result = getEventFullDtoWithStats(event);
        saveHit("/events/" + eventId, ip);
        return result;
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = getEventEntity(eventId);
        return getEventFullDtoWithStats(event);
    }

    @Override
    public List<EventFullDto> getEventsAdmin(GetEventAdminRequest request, Pageable pageable) {
        validateRangeStartAndEnd(request.getRangeStart(), request.getRangeEnd());

        Specification<Event> specification = buildAdminSpecification(request);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        if (events.isEmpty()) return List.of();

        return getEventsFullDtoWithStats(events);
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventEntity(eventId);

        if (request.getStateAction() != null) {
            handleAdminStateAction(event, request.getStateAction());
        }

        updateEventFields(event, request);

        Event updatedEvent = eventRepository.save(event);
        return getEventFullDtoWithStats(updatedEvent);
    }

    @Override
    public List<EventShortDto> getEventsPublic(GetEventPublicRequest request, Pageable pageable, String ip) {
        validateRangeStartAndEnd(request.getRangeStart(), request.getRangeEnd());

        Specification<Event> specification = buildPublicSpecification(request);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        if (events.isEmpty()) return List.of();

        saveHit("/events", ip);

        List<EventShortDto> result = getEventsShortDtoWithStats(events);
        return sortEvents(result, request.getSort());
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId, String ip) {
        Event event = getPublishedEventEntity(eventId);
        saveHit("/events/" + eventId, ip);
        return getEventFullDtoWithStats(event);
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

    private void saveHit(String path, String ip) {
        SaveHitDto endpointHitDto = new SaveHitDto("ewm-service", path, ip, LocalDateTime.now());
        statsClient.saveHit(endpointHitDto);
    }

    private Map<Long, Long> getViews(List<Long> eventIds, LocalDateTime start) {
        if (eventIds.isEmpty()) return Map.of();

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();
        LocalDateTime end = LocalDateTime.now();

        List<GetStatsDto> stats = statsClient.getStats(start, end, uris, true);

        Map<Long, Long> views = eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0L));

        if (stats != null && !stats.isEmpty()) {
            stats.forEach(stat -> {
                long eventId;
                try {
                    eventId = Long.parseLong(stat.getUri().substring("/events".length() + 1));
                } catch (Exception e) {
                    eventId = -1L;
                }
                if (eventId >= 0) {
                    views.put(eventId, stat.getHits());
                }
            });
        }
        return views;
    }

    private Category getCategoryEntity(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Category with id=%d was not found", catId)));
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id=%d was not found", userId)));
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BusinessRuleException(String.format("Field: eventDate. Error: должно содержать дату, " +
                    "которая еще не наступила. Value:%s", eventDate));
        }
    }

    private Event getEventEntity(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Event with id=%d not found", eventId)));
    }

    private void validateUserIsInitiator(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BusinessRuleException(
                    String.format("User with id=%d is not the initiator of this event", userId));
        }
    }

    private void validateEventNotPublished(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Event must not be published");
        }
    }

    private void updateEventFields(Event event, UpdateEventUserRequest updateRequest) {
        if (updateRequest.getTitle() != null)
            event.setTitle(updateRequest.getTitle());
        if (updateRequest.getDescription() != null)
            event.setDescription(updateRequest.getDescription());
        if (updateRequest.getAnnotation() != null)
            event.setAnnotation(updateRequest.getAnnotation());
        if (updateRequest.getCategory() != null) {
            Category category = getCategoryEntity(updateRequest.getCategory());
            event.setCategory(category);
        }
        if (updateRequest.getEventDate() != null)
            event.setEventDate(updateRequest.getEventDate());
        if (updateRequest.getPaid() != null)
            event.setPaid(updateRequest.getPaid());
        if (updateRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        if (updateRequest.getRequestModeration() != null)
            event.setRequestModeration(updateRequest.getRequestModeration());
        if (updateRequest.getLocation() != null) {
            event.getLocation().setLat(updateRequest.getLocation().getLat());
            event.getLocation().setLon(updateRequest.getLocation().getLon());
        }
        if (updateRequest.getStateAction() != null) {
            handleStateAction(event, updateRequest.getStateAction());
        }
    }

    private void handleStateAction(Event event, StateAction stateAction) {
        switch (stateAction) {
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

    private EventFullDto getEventFullDtoWithStats(Event event) {
        List<Long> searchEventIds = List.of(event.getId());
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(searchEventIds);
        Map<Long, Long> views = getViews(searchEventIds, event.getCreatedOn());

        return eventMapper.toFullDto(event,
                confirmedRequestsCount.getOrDefault(event.getId(), 0),
                views.getOrDefault(event.getId(), 0L));
    }

    private Specification<Event> buildAdminSpecification(GetEventAdminRequest request) {
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

        return specification;
    }

    private List<EventFullDto> getEventsFullDtoWithStats(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(eventIds);
        LocalDateTime startDate = getEarliestEventDate(events);
        Map<Long, Long> views = getViews(eventIds, startDate);

        return events.stream()
                .map(event -> eventMapper.toFullDto(
                        event,
                        confirmedRequestsCount.getOrDefault(event.getId(), 0),
                        views.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    private LocalDateTime getEarliestEventDate(List<Event> events) {
        return events.stream()
                .min(Comparator.comparing(Event::getCreatedOn))
                .orElseThrow(() -> new IllegalStateException("Events list is empty"))
                .getCreatedOn();
    }

    private void handleAdminStateAction(Event event, StateAdminAction stateAction) {
        EventState currentState = event.getState();

        if (stateAction == StateAdminAction.PUBLISH_EVENT) {
            validateEventCanBePublished(event, currentState);
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if (stateAction == StateAdminAction.REJECT_EVENT) {
            validateEventCanBeRejected(currentState);
            event.setState(EventState.CANCELED);
        }
    }

    private void validateEventCanBePublished(Event event, EventState currentState) {
        if (currentState != EventState.PENDING) {
            throw new ConflictException("Событие можно опубликовать только если оно в состоянии ожидания публикации");
        }
        if (event.getEventDate() != null && event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Дата начала события не может быть ранее чем через 1 часа(ов)");
        }
    }

    private void validateEventCanBeRejected(EventState currentState) {
        if (currentState == EventState.PUBLISHED) {
            throw new ConflictException("Событие можно отклонить пока оно не опубликовано");
        }
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest request) {
        if (request.getAnnotation() != null)
            event.setAnnotation(request.getAnnotation());
        if (request.getPaid() != null)
            event.setPaid(request.getPaid());
        if (request.getEventDate() != null)
            event.setEventDate(request.getEventDate());
        if (request.getDescription() != null)
            event.setDescription(request.getDescription());
        if (request.getTitle() != null)
            event.setTitle(request.getTitle());
        if (request.getParticipantLimit() != null)
            event.setParticipantLimit(request.getParticipantLimit());
        if (request.getCategory() != null) {
            Category category = getCategoryEntity(request.getCategory());
            event.setCategory(category);
        }
    }

    private Specification<Event> buildPublicSpecification(GetEventPublicRequest request) {
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

        return specification;
    }

    private List<EventShortDto> sortEvents(List<EventShortDto> events, SortState sort) {
        if (sort == null) return events;

        switch (sort) {
            case VIEWS:
                return events.stream()
                        .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                        .toList();
            case EVENT_DATE:
                return events.stream()
                        .sorted(Comparator.comparing(EventShortDto::getEventDate))
                        .toList();
            default:
                return events;
        }
    }

    private List<EventShortDto> getEventsShortDtoWithStats(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Integer> confirmedRequestsCount = getConfirmedRequests(eventIds);
        LocalDateTime startDate = getEarliestEventDate(events);
        Map<Long, Long> views = getViews(eventIds, startDate);

        return events.stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        confirmedRequestsCount.getOrDefault(event.getId(), 0),
                        views.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    private Event getPublishedEventEntity(Long eventId) {
        return eventRepository.findById(eventId)
                .filter(ev -> ev.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));
    }
}