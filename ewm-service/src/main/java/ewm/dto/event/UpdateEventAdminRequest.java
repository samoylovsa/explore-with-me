package ewm.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventRequest {
    private AdminStateAction stateAction;
}
