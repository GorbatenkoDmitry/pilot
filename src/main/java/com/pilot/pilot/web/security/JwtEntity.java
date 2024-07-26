package com.pilot.pilot.web.security;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

//Здесь мы будем хранить пользователя нужного для спринга
//то есть у нас есть наше представление,но нужно его показать спрингу
//у спринга уже есть свой класс UserDeteeils имплеенитируем его методы
//надо реализовать методы и что то возрващать
@Data
@AllArgsConstructor
public class JwtEntity implements UserDetails {

// так же мы добавим данные, котоыре будем здесь хранить
     private Long id;
    private final String username;
    private final String name;
     private final String password;
     // коллекция ролей и полномочий котоырй имееет этот пользовательн дл яспринга
    // это для того что бы мы могли переопределять методы GrantedAuthority
     private final Collection<? extends GrantedAuthority> authorities;



    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
