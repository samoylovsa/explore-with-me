package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitDto {
    private Long id;

    @NotNull(message = "В запросе должен пристутствовать идентифактор сервиса")
    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    private String app;

    @NotNull(message = "В запросе должен пристутствовать URI")
    @NotBlank(message = "URI не может быть пустым")
    private String uri;

    @NotNull(message = "В запросе должен пристутствовать ip-адрес")
    @NotBlank(message = "ip-адрес не может быть пустым")
    private String ip;

    @PastOrPresent
    private LocalDateTime timestamp;
}
