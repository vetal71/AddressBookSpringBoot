package ru.javabegin.training.javafx.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.javabegin.training.javafx.entity.Person;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {

    List<Person> findByFioContainingIgnoreCase(String fio);

    Page<Person> findByFioContainingIgnoreCase(String fio, Pageable pageable);

}
