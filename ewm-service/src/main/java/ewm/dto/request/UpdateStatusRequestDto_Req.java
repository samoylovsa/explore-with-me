package ewm.dto.request;

import ewm.model.request.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDto_Req {
    List<Long> requestIds;
    RequestStatus status;
}
