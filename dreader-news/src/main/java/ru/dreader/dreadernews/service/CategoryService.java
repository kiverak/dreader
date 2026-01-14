package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.CategoryDto;
import ru.dreader.dreadernews.dto.ChannelDto;
import ru.dreader.dreadernews.entity.Category;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.repo.CategoryRepository;
import ru.dreader.mvc.exception.ResourceNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryDto(category.getId(), category.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return new CategoryDto(category.getId(), category.getName());
    }

    @Transactional(readOnly = true)
    public CategoryDto getByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return new CategoryDto(category.getId(), category.getName());
    }

    @Transactional
    public CategoryDto create(String name) {
        try {
            getByName(name);
            throw new IllegalArgumentException("Category with name " + name + " already exists");
        } catch (Exception ignore) {}
        Category category = new Category();
        category.setName(name);
        category = categoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getName());
    }

    @Transactional
    public CategoryDto update(Long id, String name) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setName(name);
        category = categoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getName());
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
