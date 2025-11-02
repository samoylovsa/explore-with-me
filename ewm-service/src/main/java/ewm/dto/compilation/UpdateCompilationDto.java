package ewm.dto.compilation;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationDto {
    Set<Long> events;

    Boolean pinned;

    @Size(min = 1, max = 50, message = "Заголовок должен содержать от {min} до {max} символов")
    String title;
}
