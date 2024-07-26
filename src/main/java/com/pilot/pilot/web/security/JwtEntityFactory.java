package com.pilot.pilot.web.security;

import com.pilot.pilot.domain.user.Role;
import com.pilot.pilot.domain.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//этот класс будет реализовыывать паттерн фактори
//тут мы будем создавать из user jwtEntity
public class JwtEntityFactory {
    //сощдадим метод create и передадим туда юзера
    public static JwtEntity create(final User user) {
        return new JwtEntity(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getPassword(),
                mapToGrantedAuthorities(new ArrayList<>(user.getRoles()))
        );
    }
        //так же создадим метод mapGrantedAuthorities который будет  возвращать лист
    // GrantedAuthorities
    //тут передаем енум и мэппим на спринг,что бы он понимал наши енумы и уже давал пользователю праава

    private static List<GrantedAuthority> mapToGrantedAuthorities(final List<Role> roles) {
        return roles.stream()
                //мэппим енум по енум имени
                .map(Enum::name)
                //и мэппим на SimpleGrantedAuthority, который принимает название роли в качестве параметры
                //и мы это меппим сюда и создаем объект
                .map(SimpleGrantedAuthority::new)
                //и коллект коллекторс ту лист
                .collect(Collectors.toList());
    }

}