package ewm.dto.event;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GetEventPublicRequest {

            String text;

            List<Long> categories;

            Boolean paid;

            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeStart;

            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeEnd;

            Boolean onlyAvailable;

            SortState sort;

            Integer from;

            Integer size;

    public GetEventPublicRequest() {
        if (onlyAvailable == null) {
            onlyAvailable = false;
        }
        if (sort == null) {
            sort = SortState.EVENT_DATE;
        }
        if (from == null) {
            from = 0;
        }
        if (size == null) {
            size = 10;
        }
    }
}
