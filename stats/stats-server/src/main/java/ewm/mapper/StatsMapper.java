package ewm.mapper;

import dto.GetStatsDto;
import dto.SaveHitDto;
import ewm.model.Stats;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<GetStatsDto> mapToGetStatsDto(List<Object[]> results) {
        return results.stream()
                .map(this::mapObjectArrayToDto)
                .collect(Collectors.toList());
    }

    public GetStatsDto mapObjectArrayToDto(Object[] result) {
        return GetStatsDto.builder()
                .app((String) result[0])
                .uri((String) result[1])
                .hits((Long) result[2])
                .build();
    }
}
