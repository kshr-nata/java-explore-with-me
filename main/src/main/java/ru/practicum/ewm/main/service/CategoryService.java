package ru.practicum.ewm.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.dto.NewCategoryDto;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.mapper.CategoryMapper;
import ru.practicum.ewm.main.model.Category;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    public Category create(NewCategoryDto newCategoryDto) {
        return categoryRepository.save(CategoryMapper.mapToCategory(newCategoryDto));
    }

    public void deleteById(int id) {
        Category category = categoryRepository.findById(id).orElseThrow(()
                -> new NotFoundException(String.format("Category with id=%d was not found",
                id)));
        if (!eventRepository.findByCategoryId(id).isEmpty()) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.deleteById(id);
    }

    public Category update(Integer id, NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(()
                -> new NotFoundException(String.format("Category with id=%d was not found",
                id)));
        category.setName(newCategoryDto.getName());
        return categoryRepository.save(category);
    }

    public Category getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found",
                        categoryId)));
    }

    public List<Category> getAllCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        return categoryRepository.findAll(pageable).getContent();
    }

}
