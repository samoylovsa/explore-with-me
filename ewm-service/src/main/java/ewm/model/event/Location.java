package ewm.model.event;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Location {
    private Double lat;
    private Double lon;
}
