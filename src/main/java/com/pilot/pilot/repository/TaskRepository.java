package com.pilot.pilot.repository;

import com.pilot.pilot.domain.task.Task;

import java.util.List;
import java.util.Optional; // Класс оптинал это обертка вокруг класса и позволяет работать с нулевыми ссылками

public interface TaskRepository {

    Optional<Task> findById(Long id);
    List<Task> findAllByUserId();
    void assignToUserById(Long taskId, Long userId);//метод связывает задачу и пользователя.
    void update(Task task);
    void create(Task task);
    void delete(Long id);



}
