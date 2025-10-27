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
    @NotNull
    @PastOrPresent
    LocalDateTime start;

    @NotNull
    @PastOrPresent
    LocalDateTime end;

    List<String> uris;

    Boolean unique;
}
