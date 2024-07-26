package com.pilot.pilot.web.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.GenericFilterBean;
//GenericFilterBean это один из юинов фильтра
@AllArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {
//помещаем сюда jwt токен провайдер
    private final JwtTokenProvider jwtTokenProvider;
// имплементим doFilter, который будет выполнять всю логику
    @Override
    @SneakyThrows
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain
    ) {//вначале он будет принимать токин из хидера в  bearerToken, ( мы токин поместили   в
        //http запрос в хеадер в Authorization
        String bearerToken = ((HttpServletRequest) servletRequest)
                .getHeader("Authorization");
        //даллее проверка если этот токен не равен нулл  и он начинается со слова Bearer "
        //то мы Bearer " убираем и с 7 позиции вырезаем
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken = bearerToken.substring(7);
        }

        try {
            //если токен не нулл и он валидный
            if (bearerToken != null
                    && jwtTokenProvider.isValid(bearerToken)) {
                ///получаем аутентификацию
                Authentication authentication
                        = jwtTokenProvider.getAuthentication(bearerToken);
                //и если аутентификация не равна нулл,то уже вызываем SecurityContextHolder это класс
                //спринг секьюрити и сетаем в getContext
                //и спринг понимает,что мы авторизовали пользователя и ему не надо ничего делать
                if (authentication != null) {
                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
        } catch (Exception ignored) {
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

}