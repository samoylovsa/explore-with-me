package ewm.repository.event;

import ewm.dto.event.AdminEventParameters;
import ewm.model.event.Event;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomEventRepository {
    List<Event> findAdminEvents(AdminEventParameters adminEventParameters, Pageable pageable);
}
