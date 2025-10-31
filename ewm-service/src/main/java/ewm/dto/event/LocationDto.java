package ewm.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocationDto {

    @NotNull(message = "Latitude cannot be empty")
    private Double lat;

    @NotNull(message = "Longitude cannot be empty")
    private Double lon;
}
