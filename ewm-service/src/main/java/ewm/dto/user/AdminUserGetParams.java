package ewm.dto.user;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminUserGetParams {
    private List<Integer> ids;

    @Min(0)
    private int from = 0;

    @Min(1)
    private int size = 10;
}
