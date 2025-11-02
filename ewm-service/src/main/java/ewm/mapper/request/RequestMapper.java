package ewm.mapper.request;

import ewm.dto.request.UserRequestDto;
import ewm.model.request.Request;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {
    public Request toEntity(UserRequestDto userRequestDto) {
        Request request = new Request();
        request.setId(userRequestDto.getId());
        request.setRequesterId(userRequestDto.getRequester());
        request.setCreated(userRequestDto.getCreated());
        request.setEventId(userRequestDto.getEvent());
        request.setStatus(userRequestDto.getStatus());
        return request;
    }

    public UserRequestDto toDto(Request request) {
        return new UserRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEventId(),
                request.getRequesterId(),
                request.getStatus()
        );
    }
}
