package com.pilot.pilot.service.impl;

import com.pilot.pilot.domain.user.User;
import com.pilot.pilot.service.UserService;

public class UserServiceImpl implements UserService{
    @Override
    public User getById(Long id) {
        return null;
    }

    @Override
    public User getByUsername(String username) {
        return null;
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public User create(User user) {
        return null;
    }

    @Override
    public boolean isTaskOwner(Long userId, Long taskId) {
        return false;
    }

    @Override
    public void delete(Long id) {

    }
}
