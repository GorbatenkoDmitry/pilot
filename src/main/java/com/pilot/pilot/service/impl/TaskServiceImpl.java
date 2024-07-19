package com.pilot.pilot.service.impl;

import com.pilot.pilot.domain.task.Task;
import com.pilot.pilot.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service

public class TaskServiceImpl implements TaskService {

    @Override
    public Task getById(Long Id) {
        return null;
    }

    @Override
    public List<Task> getAllByUserId(Long Id) {
        return null;
    }

    @Override
    public Task update(Task task) {
        return null;
    }

    @Override
    public Task create(Task task, Long id) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
