package ewm.repository.request;

import ewm.dto.request.CountConfirmedRequestsByEventId;
import ewm.model.request.Request;
import ewm.model.request.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByEventIdAndRequesterId(Long eventId, Long userId);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    @Query("""
            SELECT NEW ewm.dto.request.CountConfirmedRequestsByEventId(r.eventId, COUNT(r))
            FROM Request r
            WHERE r.eventId IN :eventIds AND r.status = ewm.model.request.RequestStatus.CONFIRMED
            GROUP BY r.eventId"""
    )
    List<CountConfirmedRequestsByEventId> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

}
