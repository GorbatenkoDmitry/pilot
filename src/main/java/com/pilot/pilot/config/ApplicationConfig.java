package com.pilot.pilot.config;

import com.pilot.pilot.web.security.JwtTokenFilter;
import com.pilot.pilot.web.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mapstruct.BeanMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//Это фаил ApplicationContext — это главный интерфейс в Spring-приложении,
// который предоставляет информацию о конфигурации приложения.
//Бины хранятися в спринг DI и его не заменить на похожий. И здесь очень важно понять —
// ценность Spring DI не в самом факте его наличия,
// а в его фундаментальности.
// Все библиотеки в экосистеме Spring, по сути, просто регистрируют свои бины в этом контейнере
//  через иньекцию зависимостей
//  разработчики приложения смогут получить нужные компоненты.
//  Простой пример: при использовании Spring Security OAuth
//  если сконфигурить параметры OAuth в application.properties,
//  то Spring Security предоставит бин OAuth2RestTemplate
//  который мы можем просто заинжектить в своем коде
// или из Configuration  @EnableWebSecurity.Последний маркирует Spring Security и таким образом можно
// определить конфигурацию Spring Security
//Она вбирает в себя и хорошо известную аннотацию @Configuration и @EnableGlobalAuthentication
//(помечает, что класс может быть использован для построения экземпляра AuthenticationManagerBuilder - строитель того, что используют фильтры, о которых идёт здесь речь).
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApplicationConfig {
    //создаем переменную типа  ApplicationContext, раз файнал при создании экземпляра ApplicationConfig
    // Она должна быть проинициализирована
private final JwtTokenProvider tokenProvider;
    private final ApplicationContext applicationContext;

    //Начнем с определения простого BCryptPasswordEncoder как компонента в нашей конфигурации:
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager выполняет аутентификацию LDAP с использованием аутентификации привязки:
//Мы используем свой authenticationManager, чтобы настроить успешную аутентификацию для одного пользователя с жестко определённым логином и паролем (для тестового примера этого будет достаточно). Выглядит это так:
@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
}
//В Spring Security 5.4  представили возможность настройки HttpSecurity путем создания
// SecurityFilterChainbean-компонента.Он позволяет настраивать веб-безопасность для определенных http-запросов.
// По умолчанию он будет применяться ко всем запросам, но может быть ограничен
// с помощью #requestMatcher(RequestMatcher)или другими похожими методами.
    //поэтому создаем бин, который принимает HttpSecurity

//Сначала нам нужно отключить конфигурацию Spring Security по умолчанию от Spring Boot. Для этого достаточно просто объявить бин securityFilterChain:
    Здесь мы просто отключили CSRF (для production лучше включить и настроить, скорее всего), в тестовом примере он нам будет только мешать.
//Внутри этого метода мы должны сконфигурировать SecurityFilterChain на основе HttpSecurity, используя его методы authorizeHttpRequests:
    //С помощью методов authorizeHttpRequests мы настраиваем различные доступы к URL. Например, 
    //к /rest/v1/login разрешены все запросы ( permitAll), а к всем остальным URL внутри /rest будут иметь только пользователи с ролью ROLE_USER ( hasRole("USER")).

    @Bean
    @SneakyThrows
    public SecurityFilterChain filterChain(
            final HttpSecurity httpSecurity
    ) {
        httpSecurity
            //это стандартные фильтры Если все эти фильтры выключить, то все ресурсы будут доступны для всех запросов. Ну т.е. проверки можно сказать теперь нет.
            ///Сross Site Request Forgery) в переводе на русский — это подделка межсайтовых запросов.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(configurer ->
                        configurer.authenticationEntryPoint(
                                        (request, response, exception) -> {
                                            response.setStatus(
                                                    HttpStatus.UNAUTHORIZED
                                                            .value()
                                            );
                                            response.getWriter()
                                                    .write("Unauthorized.");
                                        })
                                .accessDeniedHandler(
                                        (request, response, exception) -> {
                                            response.setStatus(
                                                    HttpStatus.FORBIDDEN
                                                            .value()
                                            );
                                            response.getWriter()
                                                    .write("Unauthorized.");
                                        }))
            ///авторизованный хттп запрос
                .authorizeHttpRequests(configurer ->
                        configurer.requestMatchers("/api/v1/auth/**")
                                .permitAll()
                                       //таким образом мы говорим что бы за сваггером любая тсраница была доступна
                                .requestMatchers("/swagger-ui/**")
                                .permitAll()
                                .requestMatchers("/v3/api-docs/**")
                                .permitAll()
                                .requestMatchers("/graphiql")
                                .permitAll()
                                .anyRequest().authenticated()) ///производьный и другой запрос через authenticated
                .anonymous(AbstractHttpConfigurer::disable)
            //доболвяем фильтр перед 
                .addFilterBefore(new JwtTokenFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

}
