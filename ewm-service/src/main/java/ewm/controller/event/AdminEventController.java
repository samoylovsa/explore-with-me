package ewm.controller.event;

import ewm.dto.event.AdminEventParameters;
import ewm.dto.event.EventFullDto;
import ewm.dto.event.UpdateEventAdminRequest;
import ewm.model.event.EventState;
import ewm.service.event.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getAdminEvents(@RequestParam(required = false) List<Long> users,
                                             @RequestParam(required = false) List<String> states,
                                             @RequestParam(required = false) List<Long> categories,
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                             @RequestParam(required = false) LocalDateTime rangeStart,
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                             @RequestParam(required = false) LocalDateTime rangeEnd,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        List<EventState> eventStates = new ArrayList<>();
        if (states != null) {
            states.forEach(state -> eventStates.add(EventState.valueOf(state)));
        }
        AdminEventParameters adminEventParameters = AdminEventParameters.builder()
                .users(users)
                .states(eventStates)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
        log.info("Received admin get events request: {}", adminEventParameters);
        return eventService.getAdminEvents(adminEventParameters);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateAdminEvent(@PathVariable Long eventId,
                                         @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Received request: {} to admin update event with id {}", updateEventAdminRequest, eventId);
        return eventService.updateAdminEvent(eventId, updateEventAdminRequest);
    }
}
