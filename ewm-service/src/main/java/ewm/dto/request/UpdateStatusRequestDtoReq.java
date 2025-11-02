package ewm.dto.request;

import ewm.model.request.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDtoReq {
    List<Long> requestIds;
    RequestStatus status;
}
