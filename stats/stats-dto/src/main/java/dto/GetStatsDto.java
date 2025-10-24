package dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class GetStatsDto {
    private String app;

    private String uri;

    private Integer hits;
}
