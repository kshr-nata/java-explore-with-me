package ru.practicum.ewm.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.dto.NewCategoryDto;
import ru.practicum.ewm.main.model.Category;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryMapper {
    public static Category mapToCategory(NewCategoryDto newCategoryDto) {
        Category category = new Category();
        category.setName(newCategoryDto.getName());
        return category;
    }
}
