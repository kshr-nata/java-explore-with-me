package ru.practicum.ewm.main.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.NewCategoryDto;
import ru.practicum.ewm.main.model.Category;
import ru.practicum.ewm.main.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Autowired
    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public Category create(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.create(newCategoryDto);
    }

    @DeleteMapping("{catId}")
    public void deleteById(@PathVariable long catId) {
        categoryService.deleteById(catId);
    }

    @PatchMapping("{catId}")
    public Category updateById(@PathVariable long catId, @Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.update(catId, newCategoryDto);
    }
}
