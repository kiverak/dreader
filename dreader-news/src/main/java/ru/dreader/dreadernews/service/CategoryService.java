package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.CategoryDto;
import ru.dreader.dreadernews.entity.Category;
import ru.dreader.dreadernews.enums.Language;
import ru.dreader.dreadernews.repo.CategoryRepository;
import ru.dreader.mvc.debugLogging.DebugLog;
import ru.dreader.mvc.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @DebugLog
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#root.method.name")
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryDto(category.getId(), category.getName(), category.getLang().getCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#id")
    public CategoryDto getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return new CategoryDto(category.getId(), category.getName(), category.getLang().getCode());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#ids")
    public List<Category> getCategoriesByIds(List<Long> ids) {
        return categoryRepository.findAllById(ids);
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
    @CacheEvict(value = "categories", key = "#id")
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setName(dto.name());
        category.setLang(Language.fromCode(dto.lang().toLowerCase()));
        category = categoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getName(), category.getLang().getCode());
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#id")
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
