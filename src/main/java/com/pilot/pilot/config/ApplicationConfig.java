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
// Configuration и  @EnableWebSecurity вместе используются для настройки WebSecurity 
//Один @EnableWebSecurity  говорит что данный класс применяется для глобальной безопасности приложения.

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

//По факту после подключения спринг секьюрити у нас стоит единственный фильтр по умолчанию ( в него с помощью метода  InMemoryUserDeteilsManeger можно в ручную передать объекты юзера с паролями и логинами, но это не удобно и статично) и он запрещает 
    //доступ к любому контенту, короче это реализация для школы.
    //что бы сделать свой фильр в спринг секьюрити есть спринг секьюрити фильтр чейн
    //этот метод будет принимать в себя класс HttpSecurity, который позволяет конфигирироваь аунтификацию и аворизацию 
    @Bean
    @SneakyThrows
    public SecurityFilterChain filterChain(
            final HttpSecurity httpSecurity
    ) {
        //первое что отключим в этом классе это защиту от csrf атак. Вкратце такие атаки позволяют красть атаки за счет  смены маила, повышать привелегии, приводить код в исполнение и многое другое, но с версии спринг секьюрити 4 к ответу автоматически
        //доболвяется ссрф токен, который проверяет не случилось ли подмены пользователя.
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
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
            ///здесь моделируем нашу защщиту на уровне запроса  
            //назовем как то элемент например configurer и у этого элемента вызовем requestMatchers. За аргумент он принимает строку содержащую юрл, например хочу что бы ("/api/v1/auth/**") была доступна всем. Две звездочки допускают любые пути после шаблона
            //указываю ее
                .authorizeHttpRequests(configurer ->
                        configurer.requestMatchers("/api/v1/auth/**")
                                       // а далее добовляют permitAll таким образом мы говорим что бы за сваггером любая тсраница была доступна
                                .permitAll()
                                       
                                .requestMatchers("/swagger-ui/**")
                                .permitAll()
                                       
                                .requestMatchers("/v3/api-docs/**")
                                .permitAll()
                                       
                                .requestMatchers("/graphiql")
                                .permitAll()
                                       //остальные запросы все через аунтификацию
                                .anyRequest().authenticated()) 
            //анонимным пользователям доступ закрыть 
                .anonymous(AbstractHttpConfigurer::disable)
            //доболвяем фильтр перед 
                .addFilterBefore(new JwtTokenFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
//теперь все строим
        return httpSecurity.build();
    }

}
