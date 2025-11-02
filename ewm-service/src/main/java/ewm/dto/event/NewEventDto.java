package ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NewEventDto {

    @NotBlank(message = "Annotation cannot be empty")
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull(message = "Category cannot be empty")
    private Long category;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(message = "Title cannot be empty")
    @Size(min = 3, max = 120)
    private String title;


}
