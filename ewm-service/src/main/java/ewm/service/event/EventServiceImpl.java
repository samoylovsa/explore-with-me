package ewm.service.event;

import ewm.dto.event.EventFullDto;
import ewm.dto.event.EventShortDto;
import ewm.dto.event.NewEventDto;
import ewm.dto.event.UpdateEventUserRequest;
import ewm.exception.BusinessRuleException;
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
            throw new BusinessRuleException("Event must not be published");
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
}
