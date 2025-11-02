package ewm.service.compilation;

import ewm.dto.compilation.CompilationDto;
import ewm.dto.compilation.CreateCompilationDto;
import ewm.dto.compilation.UpdateCompilationDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(CreateCompilationDto request);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationDto request);

    List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable);

    CompilationDto getCompilationDtoById(Long compId);
}
