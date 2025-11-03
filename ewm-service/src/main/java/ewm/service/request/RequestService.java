package ewm.service.request;

import ewm.dto.request.UpdateStatusRequestDto_Req;
import ewm.dto.request.UpdateStatusRequestDto_Resp;
import ewm.dto.request.UserRequestDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RequestService {

    List<UserRequestDto> getRequestsByUser(Long userId);

    UserRequestDto addRequest(Long userId, Long eventId);

    UserRequestDto cancelRequest(Long userId, Long requestId);

    UserRequestDto getRequestById(Long requestId);

    List<UserRequestDto> getRequestsByEventId(Long userId, Long eventId);

    @Transactional
    UpdateStatusRequestDto_Resp updateRequestStatus(Long userId, Long eventId, UpdateStatusRequestDto_Req request);
}
