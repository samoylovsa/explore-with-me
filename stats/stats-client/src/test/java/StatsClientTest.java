import dto.GetStatsDto;
import dto.SaveHitDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class StatsClientTest {

    private final StatsClient statsClient = new StatsClient("http://localhost:9090");

    @Test
    void testSaveHit() {
        SaveHitDto hit = SaveHitDto.builder()
                .app("ewm-service")
                .uri("/events/1")
                .ip("192.168.1.100")
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.saveHit(hit);
    }

    @Test
    void testGetStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        List<GetStatsDto> stats = statsClient.getStats(start, end);

        System.out.println("Received stats: " + stats);
    }
}
