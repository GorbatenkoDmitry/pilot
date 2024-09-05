package com.pilot.pilot.web.security.expression;

import com.pilot.pilot.domain.user.Role;
import com.pilot.pilot.service.UserService;
import com.pilot.pilot.web.security.JwtEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
/// Данный фаил содерджит в себе написанные нами секьюрити экспрешшены, выражения нужные для проверки эндпоинтов в контроллерах
//повесим компонент, что бы спринг находил и надо дать название бина cse (customsecurityexpression)

@Component("cse")

  // доавим @RequiredArgsConstructor так как будем  обращаться к сервисам

@RequiredArgsConstructor
public class CustomSecurityExpression {

    private final UserService userService;
//будем возвращать тру или фолс, может ли данный пользователь, который на данный момент авторизован обращаться к пользовтелю по переданному айди и его эндоинтам
    public boolean canAccessUser(final Long id) {
      //AuthenticationManager Его единственный метод authenticate() выполняет аутентификацию, то есть решает, действительно ли пользователь тот, за кого себя выдает. Делегируется проверка конкретным провайдерам (в зависимости от того, как хранится реальный пользователь, проверка разнится).
      //метод authenticate() получает на вход объект Authentication с именем и паролем,
///Метод authenticate(Authentication authentication) интерфейса AuthenticationManager — проверка пароля
      //В случае же успеха возвращается тоже объект Authentication, который содержит поля
      //в т.ч. Principal после успешной авторизации имя и пароль перемещаются объект Principal:
      //осле аутентификации в поле Principal объекта Authentication будет реальный пользователь в виде UserDetails:
      // и тут мы его получаем и записываем как объект JwtEntity
        JwtEntity user = getPrincipal();
      //далее у него забираем и сравниваем айди
        Long userId = user.getId();
      

        return userId.equals(id) || hasAnyRole(Role.ROLE_ADMIN);
    }

    private boolean hasAnyRole(
            final Role... roles
    ) {
      //получили ауинтификация
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        for (Role role : roles) {
            SimpleGrantedAuthority authority
                    = new SimpleGrantedAuthority(role.name());
            if (authentication.getAuthorities().contains(authority)) {
                return true;
            }
        }
        return false;
    }

    public boolean canAccessTask(
            final Long taskId
    ) {
        JwtEntity user = getPrincipal();
        Long id = user.getId();

        return userService.isTaskOwner(id, taskId);
    }

    private JwtEntity getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        return (JwtEntity) authentication.getPrincipal();
    }

}
