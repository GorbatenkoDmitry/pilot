package com.pilot.pilot.service.impl;

import com.example.tasklist.domain.MailType;
import com.example.tasklist.domain.exception.ResourceNotFoundException;
import com.example.tasklist.domain.user.Role;
import com.example.tasklist.domain.user.User;
import com.example.tasklist.repository.UserRepository;
import com.example.tasklist.service.MailService;
import com.example.tasklist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Properties;
import java.util.Set;
//userservice  нам нуджен для взаимодейтсвия с данными пользователя
@Service
   // транзакшнл вешается среад онлу труе только там,где не ббудет происходит изменениц с базой данных
    //2


//Если наш метод помечен аннотацией @Transactional(readOnly = true), то мы не можем в нём (и в тех, методах, которые он вызывает) использовать операции CREAT, UPDATE, DELETE.
//Но с самими данными после выхода из метода, мы можем делать что угодно? Это правильно?
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    // нам нужен пассворд энкодер/ потому что сохранять пароль будем закодрованный 
    private final PasswordEncoder passwordEncoder;
// ну и тут мы добовляем маил сервис 
    private final MailService mailService;
    далле имплементированные сервисы из интерфейса юзер сервис мы переопределяаем
Что делает Spring Cache? Spring Cache просто кэширует возвращаемый результат для определённых входных параметров
    @Override
    @Cacheable(
            value = "UserService::getById",
            key = "#id"
    )
    public User getById(
            final Long id
    ) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));
    }
//Что делает Spring Cache? Spring Cache просто кэширует возвращаемый результат 
    //для определённых входных параметров
//так как у нас приходит несколько параметров то у нас для это есть валуе-знанеие 
    //и ключ 
    //в данном примире в юзерсервисе в методее гетбайюзернейм 
    //кэшируем парамаетр юзернейм который возвращенн будет 
    @Override
    @Cacheable(
            value = "UserService::getByUsername",
            key = "#username"
    )
    public User getByUsername(
            final String username
    ) {

        
//в данном методе мы передали метод файндбайюзернейм класса юзеррепозиторий
        //иначне выдаем ошибку юзер не нейден

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));
    }

    @Override
    @Transactional

   /// Бывают ситуации, когда мы хотим кэшировать возвращаемое значение для какой-то 
   // сущности, но в то же время, нам нужно обновить кэш. Для таких нужд существует 
///аннотация @CachePut. Оно пропускает приложение в метод
 //   , при этом, обновляя кэш для возвращаемого значения, даже если оно уже закэшировано.
  //в данном методе мы два параметра кэшируем
    @Caching(put = {
            @CachePut(
                    value = "UserService::getById",
                    key = "#user.id"
            ),
            @CachePut(
                    value = "UserService::getByUsername",
                    key = "#user.username"
            )
    })
    public User update(
            final User user
    ) {
        //тут мы в перееменную экзистинг передаем результат гет бай айди?
        //которорй в свою очередь передаем результат метода гет айди у класса юзер
        User existing = getById(user.getId());
        //в экзистинг прописываем имя через метод сет нейм 
        existing.setName(user.getName());
       // в юзер мы вставляем юзер нейм после получения из юзера метода гет юзернейма
        //и так же с паролем
        user.setUsername(user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        а затем передаем репозиторию методу сейв
        userRepository.save(user);
        return user;
    }
///Аналогично делаем с с классом создать пользователя 
    //тразакция в спринг это алгоритм действий с базой подключится и т/д/ и другие действия 
    //и если что то пошло не. так тогда выдается эксепшн
    //Транзакция — это архив для запросов к базе. Он защищает ваши данные благодаря принципу «всё, или ничего».
///Представьте, что вы решили послать другу 10 файликов в мессенджере. Какие есть варианты:
//Кинуть каждый файлик отдельно.
//Сложить их в архив и отправить архив.
//Вроде бы разницы особой нет. Но что, если что-то пойдет не так? Соединение оборвется на середине, сервер уйдет в ребут или просто выдаст ошибку...
//В первом случае ваш друг получит 9 файлов, но не получит один.
    // а в последнем наобоорот все ил иничего то есть если чтоо то пошло не так
    //то он не получит ничего
    @Override
    @Transactional
    @Caching(cacheable = {
            @Cacheable(
                    value = "UserService::getById",
                    condition = "#user.id!=null",
                    key = "#user.id"
            ),
            @Cacheable(
                    value = "UserService::getByUsername",
                    condition = "#user.username!=null",
                    key = "#user.username"
            )
    })
    public User create(
            final User user
    ) {
        //в данном случае передали данные польователя 
     // тут если у нас есть пользователь имя его в бд
        //а делается это вызывав метод файн бай юзернейм 
        //бросается эксепшн
        //паорли вводятся дважды и если не равны?то тоже бросается экспшн
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalStateException("User already exists.");
        }
        if (!user.getPassword().equals(user.getPasswordConfirmation())) {
            throw new IllegalStateException(
                    "Password and password confirmation do not match."
            );
        }
        //дальшее если все ок вставляем данные через сет пассворд в объект юзер
        //через энкодер
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = Set.of(Role.ROLE_USER);
        user.setRoles(roles);
     //   схраняем все через юзеррепозиторий
        userRepository.save(user);
     // вызваем метод отправки ееила и передаем параметры
        mailService.sendEmail(user, MailType.REGISTRATION, new Properties());
        //возвращаем пользователя
        return user;
    }

    @Override
    @Cacheable(
            value = "UserService::isTaskOwner",
            key = "#userId + '.' + #taskId"
    )
    public boolean isTaskOwner(
            final Long userId,
            final Long taskId
    ) {
        return userRepository.isTaskOwner(userId, taskId);
    }

    @Override
    @Cacheable(
            value = "UserService::getTaskAuthor",
            key = "#taskId"
    )
    public User getTaskAuthor(
            final Long taskId
    ) {
        return userRepository.findTaskAuthor(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));
    }

    @Override
    @Transactional
    @CacheEvict(
            value = "UserService::getById",
            key = "#id"
    )
    public void delete(
            final Long id
    ) {
        userRepository.deleteById(id);
    }

}
