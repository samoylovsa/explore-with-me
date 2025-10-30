package stats.server.service;

import dto.GetStatsDto;
import dto.SaveHitDto;
import dto.StatsRequest;
import stats.server.exception.ValidationException;
import stats.server.mapper.StatsMapper;
import stats.server.model.Stats;
import stats.server.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    public void saveStats(SaveHitDto requestBody) {
        Stats stats = statsMapper.toEntity(requestBody);
        Stats savedStats = statsRepository.save(stats);
        log.info("Stats saved successfully with id: {}", savedStats.getId());
    }

    @Override
    public List<GetStatsDto> getStats(StatsRequest statsRequest) {
        validateTimeRange(statsRequest);

        List<GetStatsDto> result;
        if (Boolean.TRUE.equals(statsRequest.getUnique())) {
            result = statsRepository.findUniqueStatsGrouped(
                    statsRequest.getStart(),
                    statsRequest.getEnd(),
                    statsRequest.getUris()
            );
        } else {
            result = statsRepository.findStatsGrouped(
                    statsRequest.getStart(),
                    statsRequest.getEnd(),
                    statsRequest.getUris()
            );
        }

        log.info("Found {} stats records", result.size());
        return result;
    }

    private void validateTimeRange(StatsRequest statsRequest) {
        if (statsRequest.getStart().isAfter(statsRequest.getEnd())) {
            throw new ValidationException("Start date must be before end date");
        }
        if (statsRequest.getStart().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Start date cannot be in the future");
        }
    }
}
