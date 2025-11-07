package ewm.controller.event;

import ewm.dto.event.EventFullDto;
import ewm.dto.event.EventShortDto;
import ewm.dto.event.GetEventPublicRequest;
import ewm.service.event.EventService;
import jakarta.servlet.http.HttpServletRequest;
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
    public List<EventShortDto> getEventsPublic(@ModelAttribute @Valid GetEventPublicRequest request, HttpServletRequest httpRequest) {
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 10;
        int from = request.getFrom() != null ? request.getFrom() : 0;
        PageRequest pageRequest = PageRequest.of(from / size, size);
        log.debug("getEventsPublic request = {}", request);
        // получаем ip арес вызова сервиса
        String ip = httpRequest.getRemoteAddr();
        return eventService.getEventsPublic(request, pageRequest, ip);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventByIdPublic(@PathVariable("id") Long eventId, HttpServletRequest request) {
        log.debug("getEventByIdPublic eventId = {}", eventId);
        // получаем ip арес вызова сервиса
        String ip = request.getRemoteAddr();
        return eventService.getEventByIdPublic(eventId, ip);
    }
}