package ewm.mapper.compilation;

import ewm.dto.compilation.CompilationDto;
import ewm.dto.compilation.CreateCompilationDto;
import ewm.dto.event.EventShortDto;
import ewm.mapper.event.EventMapper;
import ewm.model.compilation.Compilation;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CompilationMapper {
    EventMapper eventMapper;

    public Compilation toEntity(CreateCompilationDto dto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.isPinned());
        return compilation;
    }

    public CompilationDto toDto(Compilation compilation, Set<EventShortDto> events) {

        return new CompilationDto(
                compilation.getId(),
                events,
                compilation.isPinned(),
                compilation.getTitle()
                );
    }
}
