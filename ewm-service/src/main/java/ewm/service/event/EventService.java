package ewm.service.event;

import ewm.dto.event.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto getUserEvent(Long userId, Long eventId, String ip);

    EventFullDto getEventById(Long eventId);

    List<EventFullDto> getEventsAdmin(GetEventAdminRequest request, Pageable pageable);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getEventsPublic(GetEventPublicRequest requestParams, Pageable pageable, String ip);

    EventFullDto getEventByIdPublic(Long eventId, String ip);
}
