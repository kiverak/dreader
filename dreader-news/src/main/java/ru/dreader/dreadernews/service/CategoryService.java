package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.CategoryDto;
import ru.dreader.dreadernews.entity.Category;
import ru.dreader.dreadernews.enums.Language;
import ru.dreader.dreadernews.repo.CategoryRepository;
import ru.dreader.mvc.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryDto(category.getId(), category.getName(), category.getLang().getCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return new CategoryDto(category.getId(), category.getName(), category.getLang().getCode());
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoriesByIds(List<Long> iDs) {
        return categoryRepository.findAllById(iDs);
    }

    @Transactional(readOnly = true)
    public CategoryDto getByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return new CategoryDto(category.getId(), category.getName(), category.getLang().getCode());
    }

    @Transactional
    public CategoryDto create(CategoryDto dto) {
        try {
            getByName(dto.name());
            throw new IllegalArgumentException("Category with name " + dto.name() + " already exists");
        } catch (Exception ignore) {
        }
        Category category = new Category();
        category.setName(dto.name());
        category.setLang(Language.fromCode(dto.lang()));
        category = categoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getName(), category.getLang().getCode());
    }

    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setName(dto.name());
        category.setLang(Language.fromCode(dto.lang().toLowerCase()));
        category = categoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getName(), category.getLang().getCode());
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
