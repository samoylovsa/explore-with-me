package ewm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDto_Resp {

    List<UserRequestDto> confirmedRequests;
    List<UserRequestDto> rejectedRequests;
}
