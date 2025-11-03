package ewm.controller.event;

import ewm.dto.event.EventFullDto;
import ewm.dto.event.EventShortDto;
import ewm.dto.event.NewEventDto;
import ewm.dto.event.UpdateEventUserRequest;
import ewm.dto.request.UpdateStatusRequestDto_Req;
import ewm.dto.request.UpdateStatusRequestDto_Resp;
import ewm.dto.request.UserRequestDto;
import ewm.service.event.EventService;
import ewm.service.request.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        return eventService.getUserEvents(userId, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return eventService.updateUserEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEvent(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
        return eventService.getUserEvent(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<UserRequestDto> getRequestsByEventId(@PathVariable("userId") Long userId,
                                                   @PathVariable("eventId") Long eventId) {
        return requestService.getRequestsByEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public UpdateStatusRequestDto_Resp updateRequestStatus(@PathVariable("userId") Long userId,
                                                           @PathVariable("eventId") Long eventId,
                                                           @RequestBody UpdateStatusRequestDto_Req request) {
        return requestService.updateRequestStatus(userId, eventId, request);
    }
}
