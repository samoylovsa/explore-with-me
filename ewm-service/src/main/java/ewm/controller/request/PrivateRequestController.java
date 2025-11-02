package ewm.controller.request;

import ewm.dto.request.UserRequestDto;
import ewm.service.request.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {

    private final RequestService requestService;

    @GetMapping
    public List<UserRequestDto> getRequestsByUserId(@PathVariable("userId") Long userId) {
        log.debug("Controller: getRequestsByUser userId={}", userId);
        return requestService.getRequestsByUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRequestDto addRequest(@PathVariable("userId") Long userId,
                                              @RequestParam Long eventId) {
        log.debug("Controller: addRequest userId={}, eventId={}", userId, eventId);
        return requestService.addRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public UserRequestDto cancelRequest(@PathVariable("userId") Long userId,
                                                 @PathVariable("requestId") Long requestId) {
        log.debug("Controller: cancelRequest userId={}, requestId={}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}
