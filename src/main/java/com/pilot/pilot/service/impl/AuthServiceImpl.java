package com.pilot.pilot.service.impl;

import com.pilot.pilot.domain.user.User;
import com.pilot.pilot.service.AuthService;
import com.pilot.pilot.service.UserService;
import com.pilot.pilot.web.dto.auth.JwtRequest;
import com.pilot.pilot.web.dto.auth.JwtResponse;
import com.pilot.pilot.web.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
//когда переходим сюда в логин,то мы передаем пароль и логин,юзер нейм пассворд и аутенфикатионтокену и аутентификатион менеджеру
//они пытаются достать с базы с помощью jwtuserdeteilsservice пользователя с базы, там  у нас метод loadbyusername
//возвращается спринговый пользователь с помощью нашего фактори и уже у него достается пароль с помощью бкрипта он хэшируется
//затем сравниваются два пароля
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
   // Метод authenticate(Authentication authentication) интерфейса AuthenticationManager — проверка pароля
   // Допустим, нам приходит HTTP-запрос. Прежде чем попасть в контроллер, запрос проходит через
   // цепочку фильтров. В UsernamePasswordAuthenticationFilter имя и пароль вытаскиваются из запроса.
   // Дальше надо их сравнить с реальными. Тут то вступает в дело AuthenticationManager:
    private final AuthenticationManager authenticationManager;
    private final UserService userService;//тут у нас все методы по пользователю
    private final JwtTokenProvider jwtTokenProvider;//там методы по созданию и обновлению токенов
//переропределяем метод логин
    //передаем JwtRequest с паролем и логином помеченные анатоцацией,что не могут быть пустые
    @Override
    public JwtResponse login(final JwtRequest loginRequest) {
        //создали объект у него поля айди логин пароль и токины
        JwtResponse jwtResponse = new JwtResponse();
        // у authenticationManager вызывли метод authenticate
        //Допустим, нам приходит HTTP-запрос. Прежде чем попасть в контроллер, запрос проходит через
        //  цепочку фильтров. В UsernamePasswordAuthenticationFilter имя и пароль вытаскиваются из запроса.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        //лотсали данные
                        loginRequest.getUsername(), loginRequest.getPassword())
        );
        // если авторизациф прошла можн по юзер нейму поискать через юзер сервис пользователяя
        User user = userService.getByUsername(loginRequest.getUsername());
        //засеттили данные
        jwtResponse.setId(user.getId());
        jwtResponse.setUsername(user.getUsername());
        // засетили созданные с помощью jwtTokenProvider.createAccessToken createRefreshToken токин
        jwtResponse.setAccessToken(jwtTokenProvider.createAccessToken(
                user.getId(), user.getUsername(), user.getRoles())
        );
                jwtResponse.setRefreshToken(jwtTokenProvider.createRefreshToken(
                user.getId(), user.getUsername())
        );
        return jwtResponse;
    }

    @Override
    public JwtResponse refresh(
            final String refreshToken
    ) {
        return jwtTokenProvider.refreshUserTokens(refreshToken);
    }

}