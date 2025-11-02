package ewm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountConfirmedRequestsByEventId {
    private Long eventId;
    private int countConfirmedRequests;

    public CountConfirmedRequestsByEventId(Long eventId, Long countConfirmedRequestsLong) {
        //для запроса кол-ва, там требует конструктора с лонг
        this.eventId = eventId;
        this.countConfirmedRequests = (countConfirmedRequestsLong == null) ? 0 : countConfirmedRequestsLong.intValue();
    }
}
