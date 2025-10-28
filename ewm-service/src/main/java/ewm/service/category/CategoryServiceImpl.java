package ewm.service.category;

import ewm.dto.category.CategoryDto;
import ewm.dto.category.NewCategoryDto;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.mapper.category.CategoryMapper;
import ewm.model.category.Category;
import ewm.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Category name already exists");
        }
        Category category = categoryMapper.toEntity(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = getCategoryEntity(catId);
//  Дописать после реализации eventRepository
//        boolean hasEvents = eventRepository.existsByCategoryId(catId);
//        if (hasEvents) {
//            throw new ConflictException("The category is not empty");
//        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category existingCategory = getCategoryEntity(catId);
        if (categoryDto.getName() != null && !categoryDto.getName().isBlank()) {
            existingCategory.setName(categoryDto.getName());
        }
        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return List.of();
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        return null;
    }

    private Category getCategoryEntity(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }
}
