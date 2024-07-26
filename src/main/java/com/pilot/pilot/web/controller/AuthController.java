package com.pilot.pilot.web.controller;
import com.pilot.pilot.domain.user.User;
import com.pilot.pilot.service.AuthService;
import com.pilot.pilot.service.UserService;
import com.pilot.pilot.web.dto.auth.JwtRequest;
import com.pilot.pilot.web.dto.auth.JwtResponse;
import com.pilot.pilot.web.dto.user.UserDto;
import com.pilot.pilot.web.dto.validation.OnCreate;
import com.pilot.pilot.web.dto.validator.OnCreate;
import com.pilot.pilot.web.mappers.UserMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//В этом кратком руководстве мы обсудим разницу между аннотациями @Controller и @RestController в
// Spring MVC.//
//Первую аннотацию можно использовать для традиционных контроллеров Spring, и она уже очень давно
// является частью фреймворка.
////В Spring 4.0 была введена аннотация @RestController для упрощения создания веб-сервисов RESTful.
// Это удобная аннотация, которая объединяет @Controller и @ResponseBody , что устраняет необходимость
// аннотировать каждый метод обработки запросов класса контроллера с помощью аннотации @ResponseBody .
//Контроллер аннотирован аннотацией @RestController ; поэтому @ResponseBody не требуется.
//Каждый метод обработки запросов класса контроллера автоматически сериализует возвращаемые объекты в
// HttpResponse .
@RestController
@RequestMapping("/api/v1/auth")//это путь который мы указали в ApplicationContext
//в методе requestMatchers , а так   вызвав permitAll().Этоозначает,что все запросы проверяются на авторизацию пользователя
//а это единственный метод,который доступен всем.
@RequiredArgsConstructor
@Tag(
        name = "Auth Controller",
        description = "Auth API"
)
public class AuthController {

    private final AuthService authService;//сервис, который содержит две имплементации
    // JwtResponse login(JwtRequest loginRequest);
   // JwtResponse refresh(String refreshToken);
    private final UserService userService;// сервис, в котором методы для работы с данынми пользователя
    private final UserMapper userMapper;//мэппер,в котором два метода
    // UserDto toDto(User user); первому пердается сущность(объект юзер) он трансформируетс в data transfer object
  //  User toEntity(UserDto dto); второй метод наоборот


//далее принимаем запрос по клику из формы на путь логин и возвращает JwtResponse?
// он хранит юзернейм,аксесс и рефреш токен
    @PostMapping("/login")
    public JwtResponse login(
            //Идея Bean Validation в том, чтобы определять такие правила,
            // как «Это поле не может быть null» или «Это число должно находиться в заданном диапазоне» с помощью аннотаций.
            // Это гораздо проще, чем постоянно писать условные операторы проверок.
            //Аннотация @RequestBody осуществляет преобразование входящего JSON/XML в объект Java,
            // что актуально преимущественно для POST-запросов
            @Validated @RequestBody final JwtRequest loginRequest
    ) {
        //возвращаем метод логин у authService и передаем в параметры loginRequest
        return authService.login(loginRequest);
    }

    //  по аналогии создадим логикиу регистрации
    @PostMapping("/register")
    public UserDto register(
            //здесь уже есть On crate class
            @Validated(OnCreate.class)
            @RequestBody final UserDto userDto//в юзер дто у нас есть поля ади, имя , пароль и они
            //все почена как OnCreate.class (в пеерводе вроде при создании чего либо)
            //выходит получается проверить на валидацию  поля помеченные OnCreate

    ) {
        User user = userMapper.toEntity(userDto);
        //после того как из дто перевели в ентиту,то передали уже методу create в классе userService
        User createdUser = userService.create(user);
        return userMapper.toDto(createdUser);
    }

    @PostMapping("/refresh")
    
    public JwtResponse refresh(
            //Аннотация @RequestBody осуществляет преобразование входящего JSON/XML в объект Java,
            // что актуально преимущественно для POST-запросов
            @RequestBody final String refreshToken
    ) {
        return authService.refresh(refreshToken);
    }

}