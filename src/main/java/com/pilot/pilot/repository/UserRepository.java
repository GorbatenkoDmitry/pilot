package com.pilot.pilot.repository;

import com.pilot.pilot.domain.user.Role;
import com.pilot.pilot.domain.user.User;

import java.util.Optional;
//Нулевая ссылка является источником многих проблем,
// поскольку она часто используется для обозначения отсутствия значения.
// Java SE 8 представляет новый класс, java.util.Optionalкоторый может облегчить некоторые из этих проблем.

public interface UserRepository {
// на уровне реопоситория все методы должны начинаться с файнд
    //а в сервисе уже будут методы гет
     Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);
    void update(User user);
    void creat (User user);
    void insertUserRole(Long userId, Role role);
    boolean isTaskOwner(Long userId, Long taskId);
    void delete(Long id);
}
