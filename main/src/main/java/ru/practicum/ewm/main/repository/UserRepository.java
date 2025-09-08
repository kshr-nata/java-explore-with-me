package ru.practicum.ewm.main.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    void deleteById(int id);

    List<User> findByIdIn(List<Long> ids, Pageable pageable);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

}
