package ewm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDtoResp {

    List<UserRequestDto> confirmedRequests;
    List<UserRequestDto> rejectedRequests;
}
