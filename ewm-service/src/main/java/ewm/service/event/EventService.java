package ewm.service.event;

import ewm.dto.event.*;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto getUserEvent(Long userId, Long eventId);

    List<EventFullDto> getAdminEvents(AdminEventParameters adminEventParameters);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);
}
