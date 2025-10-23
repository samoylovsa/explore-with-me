package ewm.service;

import dto.EndpointHitDto;

public interface StatsService {
    void saveStats(EndpointHitDto requestBody);
}
