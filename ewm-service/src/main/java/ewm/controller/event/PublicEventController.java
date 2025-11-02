package ewm.controller.event;

import ewm.dto.event.EventFullDto;
import ewm.dto.event.EventShortDto;
import ewm.dto.event.GetEventPublicRequest;
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
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsPublic(@ModelAttribute @Valid GetEventPublicRequest request) {
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 10;
        int from = request.getFrom() != null ? request.getFrom() : 0;
        PageRequest pageRequest = PageRequest.of(from / size, size);
        log.debug("getEventsPublic request = {}", request);
        return eventService.getEventsPublic(request, pageRequest);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventByIdPublic(@PathVariable("id") Long eventId) {
        log.debug("getEventByIdPublic eventId = {}", eventId);
        return eventService.getEventByIdPublic(eventId);
    }
}