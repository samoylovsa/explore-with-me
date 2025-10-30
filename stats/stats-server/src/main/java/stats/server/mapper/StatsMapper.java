package stats.server.mapper;

import dto.SaveHitDto;
import stats.server.model.Stats;
import org.springframework.stereotype.Component;

@Component
public class StatsMapper {

    public Stats toEntity(SaveHitDto dto) {
        return Stats.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}
