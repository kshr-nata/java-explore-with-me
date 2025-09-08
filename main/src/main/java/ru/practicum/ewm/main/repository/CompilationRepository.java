package ru.practicum.ewm.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.Compilation;


public interface CompilationRepository extends JpaRepository<Compilation, Long>  {

    // Базовые методы без загрузки событий
    Page<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);

    Page<Compilation> findAll(Pageable pageable);

}
