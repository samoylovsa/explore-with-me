package dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
public class GetStatsDto {
    private String app;
    private String uri;
    private Long hits;

    public GetStatsDto(String app, String uri, Long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }
}