package ewm.dto.event;

import ewm.dto.category.CategoryDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category; // id, name
    private Boolean paid;
    private LocalDateTime eventDate;
    private UserShortDto initiator; // id, name
    private Integer confirmedRequests; // пока 0
    private Long views; // пока 0
}
