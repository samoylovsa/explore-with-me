package ewm.service.event;

import ewm.dto.event.*;
import ewm.exception.BusinessRuleException;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.mapper.event.EventMapper;
import ewm.model.category.Category;
import ewm.model.event.Event;
import ewm.model.event.EventState;
import ewm.model.user.User;
import ewm.repository.category.CategoryRepository;
import ewm.repository.event.EventRepository;
import ewm.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User with id=%d was not found", userId)));

        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(() ->
                new NotFoundException(String.format("Category with id=%d was not found", newEventDto.getCategory())));

        validateDateTime(newEventDto.getEventDate());

        Event event = eventMapper.toEntity(newEventDto, initiator, category);
        event = eventRepository.save(event);

        return eventMapper.toFullDto(event, 0, 0);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        Page<Event> page = eventRepository.findAllByInitiatorId(userId, pageable);
        return page.stream().map(event -> eventMapper.toShortDto(event, 0, 0)).toList();
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

        updateEvent(event, updateEventUserRequest);
        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW -> {
                    if (event.getState() == EventState.PENDING) {
                        event.setState(EventState.CANCELED);
                    } else {
                        throw new BusinessRuleException("Only pending events can be canceled");
                    }
                }
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }
        event = eventRepository.save(event);
        return eventMapper.toFullDto(event, 0, 0);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Event with id=%d not found", eventId)));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException(String.format("Event with id=%d not found for user with id=%d", eventId,
                    userId));
        }
        return eventMapper.toFullDto(event, 0, 0);
    }

    @Override
    public List<EventFullDto> getAdminEvents(AdminEventParameters adminEventParameters) {
        Pageable pageable = PageRequest.of(adminEventParameters.getFrom() / adminEventParameters.getSize(),
                adminEventParameters.getSize(), Sort.by("id"));
        List<Event> events = eventRepository.findAdminEvents(adminEventParameters, pageable);
        return events.stream().map(event -> eventMapper.toFullDto(event, 0, 0)).toList();
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Event with id=%d not found", eventId)));
        updateEvent(event, updateEventAdminRequest);
        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (event.getState() != EventState.PENDING) {
                        throw new BusinessRuleException("Only pending events can be published");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState() != EventState.PENDING) {
                        throw new BusinessRuleException("Only pending events can be canceled");
                    }
                    event.setState(EventState.CANCELED);
                }
            }
        }
        return eventMapper.toFullDto(event, 0, 0);
    }

    private void validateDateTime(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BusinessRuleException(String.format("Field: eventDate. Error: должно содержать дату, " +
                            "которая еще не наступила. Value:%s",
                    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
    }

    private void updateEvent(Event event, UpdateEventRequest updateEventRequest) {
        if (updateEventRequest.getEventDate() != null) {
            validateDateTime(updateEventRequest.getEventDate());
            event.setEventDate(updateEventRequest.getEventDate());
        }
        if (updateEventRequest.getTitle() != null)
            event.setTitle(updateEventRequest.getTitle());
        if (updateEventRequest.getDescription() != null)
            event.setDescription(updateEventRequest.getDescription());
        if (updateEventRequest.getAnnotation() != null)
            event.setAnnotation(updateEventRequest.getAnnotation());
        if (updateEventRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d not found",
                            updateEventRequest.getCategory())));
            event.setCategory(category);
        }
        if (updateEventRequest.getPaid() != null)
            event.setPaid(updateEventRequest.getPaid());
        if (updateEventRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        if (updateEventRequest.getRequestModeration() != null)
            event.setRequestModeration(updateEventRequest.getRequestModeration());
        if (updateEventRequest.getLocation() != null) {
            event.getLocation().setLon(updateEventRequest.getLocation().getLon());
            event.getLocation().setLat(updateEventRequest.getLocation().getLat());
        }
    }
}
