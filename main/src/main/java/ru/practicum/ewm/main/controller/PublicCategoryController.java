package ru.practicum.ewm.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.model.Category;
import ru.practicum.ewm.main.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class PublicCategoryController {

    private final CategoryService categoryService;

    @Autowired
    public PublicCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    List<Category> getCategories(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return categoryService.getAllCategories(from, size);
    }

    @GetMapping("{catId}")
    Category getCategoryById(@PathVariable Long catId) {
        return categoryService.getCategoryById(catId);
    }
}
