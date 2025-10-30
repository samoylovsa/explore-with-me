package stats.client;

import dto.GetStatsDto;
import dto.SaveHitDto;
import dto.StatsRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class StatsClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RestClient restClient;

    @Value("${stats-server.url}")
    private String baseUrl;

    public StatsClient() {
    }

    public StatsClient(@Value("${stats-server.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("StatsClient initialized with baseUrl: {}", baseUrl);
    }

    public void saveHit(SaveHitDto saveHitDto) {
        log.info("Sending save hit request: {}", saveHitDto);

        try {
            restClient.post()
                    .uri("/hit")
                    .body(saveHitDto)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Hit saved successfully");
        } catch (Exception e) {
            log.error("Failed to save hit: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save hit", e);
        }
    }

    public List<GetStatsDto> getStats(StatsRequest statsRequest) {
        log.info("Getting stats with request: {}", statsRequest);

        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/stats/client")
                                .queryParam("start", statsRequest.getStart().format(DATE_TIME_FORMATTER))
                                .queryParam("end", statsRequest.getEnd().format(DATE_TIME_FORMATTER))
                                .queryParam("unique", statsRequest.getUnique() != null ? statsRequest.getUnique() : false);

                        if (statsRequest.getUris() != null && !statsRequest.getUris().isEmpty()) {
                            for (String uri : statsRequest.getUris()) {
                                uriBuilder.queryParam("uris", uri);
                            }
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

        } catch (Exception e) {
            log.error("Failed to get stats: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get stats", e);
        }
    }

    public List<GetStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                      List<String> uris, Boolean unique) {
        StatsRequest statsRequest = StatsRequest.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(unique)
                .build();

        return getStats(statsRequest);
    }

    public List<GetStatsDto> getStats(LocalDateTime start, LocalDateTime end) {
        return getStats(start, end, null, false);
    }

    public List<GetStatsDto> getStatsUnique(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return getStats(start, end, uris, true);
    }

    public List<GetStatsDto> getStatsForUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return getStats(start, end, uris, false);
    }
}
