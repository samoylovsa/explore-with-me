package ewm.controller.event;

import ewm.dto.event.EventFullDto;
import ewm.dto.event.GetEventAdminRequest;
import ewm.dto.event.UpdateEventAdminRequest;
import ewm.service.event.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsAdmin(@ModelAttribute @Valid GetEventAdminRequest request) {
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 10;
        int from = request.getFrom() != null ? request.getFrom() : 0;
        PageRequest pageRequest = PageRequest.of(from / size, size);
        log.debug("getEventAdmin request = {}", request);
        return eventService.getEventsAdmin(request, pageRequest);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(@PathVariable Long eventId,
                                         @RequestBody @Valid UpdateEventAdminRequest request
    ) {
        log.debug("updateEventAdmin eventId = {}, request = {}", eventId, request);
        return eventService.updateEventAdmin(eventId, request);
    }
}