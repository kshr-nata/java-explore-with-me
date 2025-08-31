package ru.practicum.ewm.main.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.model.User;
import ru.practicum.ewm.main.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

      private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
            return userRepository.save(user);
        }

        public void deleteUser(Integer userId) {
            if (!userRepository.existsById(userId)) {
                throw new NotFoundException("User with id " + userId + " not found");
            }
            userRepository.deleteById(userId);
        }

    public List<User> getUsers(List<Integer> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        if (ids != null && !ids.isEmpty()) {
            // Если указаны IDs - ищем по ним
            return userRepository.findByIdIn(ids, pageable);
        } else {
            // Если IDs не указаны - возвращаем всех пользователей
            return userRepository.findAll(pageable).getContent();
        }
    }

        public User getUserById(Integer userId) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        }

}
