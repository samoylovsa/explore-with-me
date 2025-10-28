package ewm.mapper;

import dto.SaveHitDto;
import ewm.model.Stats;
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
