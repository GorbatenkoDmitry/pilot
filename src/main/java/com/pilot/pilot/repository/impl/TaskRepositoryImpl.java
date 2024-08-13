package com.pilot.pilot.repository.impl;

import com.pilot.pilot.domain.exception.ResourceMappingException;
import com.pilot.pilot.domain.task.Task;
import com.pilot.pilot.repository.DataSourceConfig;
import com.pilot.pilot.repository.TaskRepository;
import com.pilot.pilot.repository.mappers.TaskRowMapper;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
//поставили тег @RequiredArgsConstructor потмоу что у нас переменная файнал по подключению и она заполнена должна быть


//@Repository
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {
//создали переменную
    private final DataSourceConfig dataSourceConfig;
//создали константы с запросами на подключение
    //-- t синоним таблицы tblname
    private final String FIND_BY_ID = """
            SELECT t.id              as task_id,
                   t.title           as task_title,
                   t.description     as task_description,
                   t.expiration_date as task_expiration_date,
                   t.status          as task_status
            FROM tasks t
            WHERE t.id = ?""";

    private final String FIND_ALL_BY_USER_ID = """
            SELECT t.id              as task_id,
                   t.title           as task_title,
                   t.description     as task_description,
                   t.expiration_date as task_expiration_date,
                   t.status          as task_status
            FROM tasks t
                     JOIN users_tasks ut on t.id = ut.task_id
            WHERE ut.user_id = ?""";

    private final String ASSIGN = """
            INSERT INTO users_tasks (task_id, user_id)
            VALUES (?, ?)""";

    private final String UPDATE = """
            UPDATE tasks
            SET title = ?,
                description = ?,
                expiration_date = ?,
                status = ?
            WHERE id = ?
            """;

    private final String CREATE = """
            INSERT INTO tasks (title, description, expiration_date, status)
            VALUES (?, ?, ?, ?)""";

    private final String DELETE = """
            DELETE FROM tasks
            WHERE id = ?""";

    //унаследовали методы с интерфейса и реализовали их по работе с данными из бд

    @Override
    public Optional<Task> findById(Long id) {
        //оборачиваем в тру кэч

        try {
            //создали подключеение
            Connection connection = dataSourceConfig.getConnection();
           //подготовили запрос

            PreparedStatement statement = connection.prepareStatement(FIND_BY_ID);

            //далее с помощью сет лонг получаем лонг значение параметра, он унас первый параметрв методе индекс столбца, а второе уже значение

            statement.setLong(1, id);


            try (ResultSet rs = statement.executeQuery()) {
                // и через Optional (его ставим когда может значнеие оказаться пустым, елси нет записи
                //ofNullable(T value) — создание значения Optional для объекта, который может быть нулевым (null).
                // и возвращаем рузультат прокинув данные рс (результата) через метод mapRow который возвращает все данные полей
                //и таким образом мы нашли по айди задачу и все данные по ней

                return Optional.ofNullable(TaskRowMapper.mapRow(rs));
            }
        } catch (SQLException throwables) {
            throw new ResourceMappingException("Error while finding user by id.");
        }
    }
//метод по нахождению всех задач
    @Override
    public List<Task> findAllByUserId(Long userId) {
        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_ALL_BY_USER_ID);
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                return TaskRowMapper.mapRows(rs);
            }
        } catch (SQLException throwables) {
            throw new ResourceMappingException("Error while finding all by user id.");
        }
    }

    @Override
    public void assignToUserById(Long taskId, Long userId) {
        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(ASSIGN);
            statement.setLong(1, taskId);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throw new ResourceMappingException("Error while assigning to user.");
        }
    }

    @Override
    public void update(Task task) {
        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(UPDATE);
            statement.setString(1, task.getTitle());
            if (task.getDescription() == null) {
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(2, task.getDescription());
            }
            if (task.getExpirationDate() == null) {
                statement.setNull(3, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(3, Timestamp.valueOf(task.getExpirationDate()));
            }
            statement.setString(4, task.getStatus().name());
            statement.setLong(5, task.getId());
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throw new ResourceMappingException("Error while updating task.");
        }
    }

    @Override
    public void create(Task task) {
        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(CREATE, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, task.getTitle());
            if (task.getDescription() == null) {
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(2, task.getDescription());
            }
            if (task.getExpirationDate() == null) {
                statement.setNull(3, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(3, Timestamp.valueOf(task.getExpirationDate()));
            }
            statement.setString(4, task.getStatus().name());
            statement.executeUpdate();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                rs.next();
                task.setId(rs.getLong(1));
            }
        } catch (SQLException throwables) {
            throw new ResourceMappingException("Error while creating task.");
        }
    }

    @Override
    public void delete(Long id) {
        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(DELETE);
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throw new ResourceMappingException("Error while deleting task.");
        }
    }

}
    @Override
    public void delete(Long id) {

    }
}
