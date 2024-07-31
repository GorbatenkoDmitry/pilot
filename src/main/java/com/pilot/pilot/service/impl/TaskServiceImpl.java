package com.pilot.pilot.service.impl;

import com.pilot.pilot.domain.exception.ResourceNotFoundException;
import com.pilot.pilot.domain.task.Status;
import com.pilot.pilot.domain.task.Task;
import com.pilot.pilot.domain.task.TaskImage;
import com.example.tasklist.repository.TaskRepository;
import com.example.tasklist.service.ImageService;
import com.example.tasklist.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

//везаем аннотацию сервис,что ппозволяет нам ее потом доставать из контекста винов 

@Service
    //вещаем транзакшнл. Транзакшнл это фактически пул(набор) каких то действий и если хоть одно действие из набора не выполняется,то все действия из этого набора не выполняются.
    //При этом в консксте спринга он используется для работы с БД
    //Реад онлу тру говорит,что действия с БД включают только чтение данных и нельзя изменять их или вносить данные
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
//Далеее нам надо непосредственно реализовать методы интерфейса TaskService
    //создаем объекты что бы через них обращаться к методам
    private final TaskRepository taskRepository;
    private final ImageService imageService;
//    @Cacheable аннотация маркер того что надо закинуть в кэш
    // значение и ключ в кэше нужно для указания,что именно надо записать
    @Override
    @Cacheable(
            value = "TaskService::getById",
            key = "#id"
    )
    //функция находит по айди 
    //передается в параметр айди
    //вызывается таскрепозиторий и там метод найти по айди и передается в нее айди и если не находит,то бросается эксепшн
    public Task getById(
            final Long id
    ) {
        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found."));
    }
//просто все по айди ищем
    @Override
    public List<Task> getAllByUserId(
            final Long id
    ) {
        return taskRepository.findAllByUserId(id);
    }
//все задачи на пользователя 
    //метод, который должен возвратить лист задач
    //передается период за который найти (пока не знаю откуда это появилось)
    //далее переменная сейчас и там вызываем стандартный метода класса локалдэйт
    //возвращаем метод найти все задачи в таск репозитории, при этом передавая параметр сейчас и дополнительно метод плюс,которому переданно продолжительност времени
    @Override
    public List<Task> getAllSoonTasks(
            final Duration duration
    ) {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.findAllSoonTasks(Timestamp.valueOf(now),
                Timestamp.valueOf(now.plus(duration)));
    }
//метод обновления задач 
    //передаем ему задачу на обновление
    //в переменную существующий(экзистинг) записываем результат метода getById при этом передав результат task.getId
    // и если статус не равен нулю,тогда вставляем в экзистинг сет Статус / и передали в сетстатус результат метода TODO класса енам статус, 
    //который  енам ииспользуется для маркировки 
    //иначе
    //в сет статус передаем результат метода гет статуса в классе статус
    //вставляем загаловок и т.д.
    //и сохраняем через такс репозиторий
    // при этом раз у нас это транзакшнл,то если хоть что то не вополнитея, то весь метод не выполнится
    @Override
    @Transactional
    @CachePut(
            value = "TaskService::getById",
            key = "#task.id"
    )
    public Task update(
            final Task task
    ) {
        
        Task existing = getById(task.getId());
        if (task.getStatus() == null) {
            existing.setStatus(Status.TODO);
        } else {
            existing.setStatus(task.getStatus());
        }
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setExpirationDate(task.getExpirationDate());
        taskRepository.save(task);
        return task;
    }

    @Override
    @Transactional
    @Cacheable(
            value = "TaskService::getById",
            condition = "#task.id!=null",
            key = "#task.id"
    )
    public Task create(
            final Task task,
            final Long userId
    ) {
        if (task.getStatus() == null) {
            task.setStatus(Status.TODO);
        }
        taskRepository.save(task);
        taskRepository.assignTask(userId, task.getId());
        return task;
    }

 //   @CacheEvict: Запускает выгрузку кэша.

    @Override
    @Transactional
    @CacheEvict(
            value = "TaskService::getById",
            key = "#id"
    )
    public void delete(
            final Long id
    ) {
        taskRepository.deleteById(id);
    }
//тут все более менее понятно
    // передаем айди и картинку
    //вызываем сервис имадж и метод аплоад передав картинку
    // через таск репозиторий закидываем в бд картинку.
    @Override
    @Transactional
    @CacheEvict(
            value = "TaskService::getById",
            key = "#id"
    )
    public void uploadImage(
            final Long id,
            final TaskImage image
    ) {
        String fileName = imageService.upload(image);
        taskRepository.addImage(id, fileName);
    }

}
