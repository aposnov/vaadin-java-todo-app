package com.example.todo.repository;

import com.example.todo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByCompletedFalseAndDueDateBetween(LocalDateTime start, LocalDateTime end);
    List<Todo> findByCompletedFalseAndDueDateAfter(LocalDateTime date);
    List<Todo> findByCompletedTrue();
} 