package ewm.controller.compilation;

import ewm.dto.compilation.CompilationDto;
import ewm.dto.compilation.CreateCompilationDto;
import ewm.dto.compilation.UpdateCompilationDto;
import ewm.service.compilation.CompilationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid CreateCompilationDto request) {
        log.debug("createCompilation request = {}", request);
        return compilationService.createCompilation(request);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.debug("deleteCompilation compId = {}", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @RequestBody @Valid UpdateCompilationDto request) {
        log.debug("updateCompilation compId = {}, request = {}", compId, request);
        return compilationService.updateCompilation(compId, request);
    }
}