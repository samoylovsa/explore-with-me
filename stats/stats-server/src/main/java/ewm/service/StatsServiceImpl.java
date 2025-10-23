package ewm.service;

import dto.EndpointHitDto;
import ewm.mapper.StatsMapper;
import ewm.model.Stats;
import ewm.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    public void saveStats(EndpointHitDto requestBody) {
        Stats stats = statsMapper.toEntity(requestBody);
        Stats savedStats = statsRepository.save(stats);
        log.info("Stats saved successfully with id: {}", savedStats.getId());
    }
}
