package ru.practicum.ewm.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.dto.NewUserRequest;
import ru.practicum.ewm.main.dto.UserShortDto;
import ru.practicum.ewm.main.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public User mapToUser(NewUserRequest dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        return user;
    }

    public static UserShortDto toDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }

}
