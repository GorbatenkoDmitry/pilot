package com.pilot.pilot.web.security;


import com.pilot.pilot.domain.user.User;
import com.pilot.pilot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
//создадим JwtUserDetailsService.
// Это сервис, который будет вызваться спрингом для авторизации пользователя
    @Service
    @RequiredArgsConstructor
    public class JwtUserDetailsService implements UserDetailsService {

        private final UserService userService;
// переопределяем один метод,который будет возвращать спрингового юзера
        @Override
        public UserDetails loadUserByUsername(
                final String username
        ) {
            User user = userService.getByUsername(username);
            return JwtEntityFactory.create(user);
        }

    }

// и теперь у нас есть бд с юзерами / юзер сервис,который чтото достает оотттуда /
// мы по юзернэйму получаем пользователя
// и этот юзер в виде жвтентиту
// возвращаем ентиту как представление нашего пользователя для спринг секьюити
