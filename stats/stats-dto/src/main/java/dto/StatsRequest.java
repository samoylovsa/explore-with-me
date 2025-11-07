package dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StatsRequest {
    @NotNull(message = "start не может быть пустым")
    @PastOrPresent
    LocalDateTime start;

    @NotNull(message = "end не может быть пустым")
    LocalDateTime end;

    List<String> uris;

    Boolean unique;
}
