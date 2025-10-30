package stats.server.controller;

import dto.GetStatsDto;
import dto.SaveHitDto;
import dto.StatsRequest;
import stats.server.service.StatsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveStats(@Valid @RequestBody SaveHitDto requestBody) {
        log.info("Received save stats request: {}", requestBody);
        statsService.saveStats(requestBody);
    }

    @GetMapping("/stats")
    public List<GetStatsDto> getStats(
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.info("Received stats request: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        StatsRequest statsRequest = StatsRequest.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(unique)
                .build();

        return statsService.getStats(statsRequest);
    }
}
