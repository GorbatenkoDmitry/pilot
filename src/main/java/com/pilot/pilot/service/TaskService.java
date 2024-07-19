package com.pilot.pilot.service;

import com.pilot.pilot.domain.task.Task;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface TaskService {
    Task getById(Long Id);
    List<Task> getAllByUserId(Long Id);
    Task update(Task task);
    Task create(Task task, Long id);
    void delete(Long id);

}
