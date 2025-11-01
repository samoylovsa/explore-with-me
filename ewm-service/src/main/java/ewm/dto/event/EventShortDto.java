package ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import ewm.dto.category.CategoryDto;
import ewm.dto.user.UserShortDto;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserShortDto initiator; // id, name
    private Integer confirmedRequests; // пока 0
    private Long views; // пока 0
}
