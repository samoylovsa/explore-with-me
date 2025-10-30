package stats.server.service;

import dto.GetStatsDto;
import dto.SaveHitDto;
import dto.StatsRequest;

import java.util.List;

public interface StatsService {

    void saveStats(SaveHitDto requestBody);

    List<GetStatsDto> getStats(StatsRequest statsRequest);
}
