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

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;
//принимаем запрос по клику из формы на путь логин и возвращает JwtResponse? он хранит юзернейм,аксесс и рефреш токен
    @PostMapping("/login")
    public JwtResponse login(
            @Validated @RequestBody final JwtRequest loginRequest
    ) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public UserDto register(
            @Validated(OnCreate.class)
            @RequestBody final UserDto userDto
    ) {
        User user = userMapper.toEntity(userDto);
        User createdUser = userService.create(user);
        return userMapper.toDto(createdUser);
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(
            @RequestBody final String refreshToken
    ) {
        return authService.refresh(refreshToken);
    }

}