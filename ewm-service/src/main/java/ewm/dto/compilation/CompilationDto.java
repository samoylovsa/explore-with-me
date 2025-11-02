package ewm.dto.compilation;

import ewm.dto.event.EventShortDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    Long id;
    Set<EventShortDto> events;
    boolean pinned;
    String title;
}
