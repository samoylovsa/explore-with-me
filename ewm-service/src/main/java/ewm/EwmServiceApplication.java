package ewm;

import dto.GetStatsDto;
import dto.SaveHitDto;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import stats.StatsClient;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"ewm", "stats"})
public class EwmServiceApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(EwmServiceApplication.class, args);

        StatsClient statsClient = context.getBean(StatsClient.class);
        statsClient.saveHit(new SaveHitDto(null, "ewm-main-service", "/events/1", "192.163.0.1",
                LocalDateTime.now()));
        List<GetStatsDto> stats = statsClient.getStats(LocalDateTime.now().minusSeconds(1), LocalDateTime.now().plusSeconds(2),
                List.of("/events/1"), false);
        System.out.println(stats);
    }
}