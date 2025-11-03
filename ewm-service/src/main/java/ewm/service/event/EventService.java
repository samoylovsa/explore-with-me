package ewm.service.event;

import ewm.dto.event.EventFullDto;
import ewm.dto.event.EventShortDto;
import ewm.dto.event.NewEventDto;
import ewm.dto.event.UpdateEventUserRequest;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto getEventById(Long eventId);
}
