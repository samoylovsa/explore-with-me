package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsDto {
    @NotNull(message = "В запросе должен пристутствовать идентифактор сервиса")
    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    private String app;

    @NotNull(message = "В запросе должен пристутствовать URI")
    @NotBlank(message = "URI не может быть пустым")
    private String uri;

    @PositiveOrZero
    private Integer hits;
}
