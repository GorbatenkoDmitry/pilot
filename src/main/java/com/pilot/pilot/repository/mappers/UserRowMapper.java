package com.pilot.pilot.repository.mappers;



import com.pilot.pilot.domain.task.Task;
import com.pilot.pilot.domain.user.Role;
import com.pilot.pilot.domain.user.User;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
//создаем класс UserRowMapper. Он принимает  резалтсет  из юзеррепозитория  и возвращать уже юзера
public class UserRowMapper {
//Аннотация @SneakyThrows может быть использована для бросания проверяемых исключений без их объявления в throws метода.
// Эту немного спорную возможность нужно использовать с осторожностью, конечно. Код, генерируемый Lombok,
// НЕ будет игнорировать, оборачивать, заменять и модифицировать другим способом бросаемое исключение. Он просто обманывает компилятор.
    @SneakyThrows
    public static User mapRow(ResultSet resultSet) {
        //первое что получим - это сет ролей
        Set<Role> roles = new HashSet<>();
        //делаем цикл пока у нас есть строчки , мы добовляем роль
        while (resultSet.next()) {
            roles.add(Role.valueOf(resultSet.getString("user_role_role")));
        }
        //теперь же мы возвращаемся в начало резалт сета
        resultSet.beforeFirst();
        //получаем лист тасков
        List<Task> tasks = TaskRowMapper.mapRows(resultSet);
        ////и снова к началу
        resultSet.beforeFirst();
        //ту уже если есть строчки
        if (resultSet.next()) {
            //создаем юзера и вставляем данные
            User user = new User();
            user.setId(resultSet.getLong("user_id"));
            user.setName(resultSet.getString("user_name"));
            user.setUsername(resultSet.getString("user_username"));
            user.setPassword(resultSet.getString("user_password"));
            user.setRoles(roles);
            user.setTasks(tasks);
            return user;
        }
        return null;
    }

}