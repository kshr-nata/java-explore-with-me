package ru.practicum.ewm.main.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    void deleteById(int id);

    List<User> findByIdIn(List<Long> ids, Pageable pageable);

}
