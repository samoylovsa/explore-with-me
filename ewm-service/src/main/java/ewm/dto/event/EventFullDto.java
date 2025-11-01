package ewm.dto.event;

import ewm.dto.category.CategoryDto;
import ewm.model.event.EventState;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EventFullDto {

    private Long id;
    private String title;
    private String annotation;
    private String description;

    private CategoryDto category; // {id, name}

    private Boolean paid;
    private LocalDateTime eventDate;

    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;

    private Integer participantLimit;
    private Boolean requestModeration;

    private EventState state;

    private UserShortDto initiator; // {id, name} добавить когда будет дто

    private LocationDto location;

    private Integer confirmedRequests; // пока 0
    private Long views; // пока 0
}
