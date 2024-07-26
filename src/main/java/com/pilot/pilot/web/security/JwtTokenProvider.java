package com.pilot.pilot.web.security;

import com.pilot.pilot.domain.exception.AccessDeniedException;
import com.pilot.pilot.domain.user.Role;
import com.pilot.pilot.domain.user.User;
import com.pilot.pilot.service.UserService;
import com.pilot.pilot.service.props.JwtProperties;
import com.pilot.pilot.web.dto.auth.JwtResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {
//JwtProperties объект которы будет хранить объъекты из аппликатион ямл
    private final JwtProperties jwtProperties;
// userDetailsService загружает нам получается UserDetailsService, который в свою очередь возвращает нам
  //  результат  loadUserByUsername  в виде UserDetails(а там у нас метода getAuthorities,getPasswordБ
//  getUsername
//  boolean isAccountNonExpired();
///    boolean isAccountNonLocked();
//    boolean isCredentialsNonExpired();
//    boolean isEnabled();
//}

    private final UserDetailsService userDetailsService;
    // так жже создаем переменную UserService, которая содержит в себе операции с БД таблицей ЮЗЕРС
    private final UserService userService;
    // и создали секретный ключ типа кей(спринговый класс)
    private SecretKey key;
    //создадим метод инит
    //в нем мы в переменную кей запишем результат метода С из класса Keys
    //в параметры hmacShaKeyFor передадим с помощью getSecret поле secret из jwtProperties
    // Так же результат getSecret (поля private String secret) преоброзуем в байте с помощью
    // стандартного метода getBytes класса String
    //почему именно постаокнстракт, потому что поля файнал заавтовайрятся позже чем вызовется секреткей
    // а постконтстракт вызовет уже после @RequiredArgsConstructor и автовайринга
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    //создадим функцию для создания access token пользователя и в параметры передади ей userId username roles
    public String createAccessToken(final Long userId, final String username, final Set<Role> roles) {
       //в данном мтоде в переменную типа Claims( по переводу отвечает за заявку поользователя на полученеи
        //аксесс токенаа
        //клаймс это обхект,который будет хранить информацию о пользователе
        // в самом токене
        Claims claims = Jwts.claims()
                //subject глобальная переменная в Claims
                .subject(username)
                //метод Map добовляем пару ключ значение
                .add("id", userId)
                //resolveRoles позволяет доставать сет ролей (точно так же как в ентити факторе список стрингов)
                .add("roles", resolveRoles(roles))
                //етод баилд из класса баилдер ХЗ что он делает вроде по итогу собирает токен
                .build();
        //создаем переменную validity и записываем метод now  класса Instant,
        // который   передает время сейчас которе и плюс
        // (.plus(jwtProperties.getAccess(), ChronoUnit.HOURS)
        // то есть к дате которая уже есть прибавляем время из ямла jwtProperties.getAccess()
        Instant validity = Instant.now()
                .plus(jwtProperties.getAccess(), ChronoUnit.HOURS);
        // и все это собираем
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(validity))//указываем когда токен за экспайрится
                .signWith(key)//помечаем токен с помощью ключа
                .compact();//это все собираем
        ///поздравллю мы собрали эксесс токен
    }
//создаем функцию resolveRoles куда в парметры были переданы сет ролей
    private List<String> resolveRoles(
            final Set<Role> roles
    ) {//stream класса collection Возвращает последовательный поток с этой коллекцией в качестве источника.
        return roles.stream()
                //в стриме мы маппим имена
                .map(Enum::name)
                //и затем собираем это все в лист
                .collect(Collectors.toList());
        //то есть из сет ролей мы получили списко стрингов
    }
//теперь делаем все тоже самое для рефреш токена
    //сначала создаем метод создания рефреш токена
    //он принимает только юзер айди и юзер нейм
    //потому что нет необходимости в ролях, так как для рефрше токен просто обновляет паур токенов
    // то есть он обновляет рефреш и эксесс и никак не зайдействоан в секьюрити
    public String createRefreshToken(
            final Long userId,
            final String username
    ) {
        //здесь опять создаем переменную claims, в которую помещаем  метод claims класса Jwts
        Claims claims = Jwts.claims()
                //тут мы помещаем то,что хотели бы что бы было в нашем токене
                //передаем subject юзернейм
                .subject(username)
                // так же добовляем свои данные id
                .add("id", userId)
                //и все это билдим
                .build();

        Instant validity = Instant.now()
                .plus(jwtProperties.getRefresh(), ChronoUnit.DAYS);
        // и все это собираем
       return Jwts.builder() //вызываем метод сборщик
                .claims(claims)//данные, которые мы прописали в токене
                .expiration(Date.from(validity))//даты валидности
                .signWith(key)//ключ
                .compact();//и все скомпонавали
    }

    //теперь напишем метод,котоый нам будет обновлять пару токенов, то есть пользователь отправляет рефреш
    //токен и получает пару токенов
    // принимает в качестве входного параметра   refreshToken
    public JwtResponse refreshUserTokens(final String refreshToken) {
        //здесь сохдается объект jwtResponse
        JwtResponse jwtResponse = new JwtResponse();
        //далее делаем проверку и если токен не валидный мы выбрасываем  исключенеи наше AccessDeniedException
        if (!isValid(refreshToken)) {
            throw new AccessDeniedException();
        }
        //иначе если все хорошо создаем перемеенную, парсим токер и достаем оттуда айди
        Long userId = Long.valueOf(getId(refreshToken));
        //достаем юзера по айди
        User user = userService.getById(userId);
        //помещаем данные в jwtResponse
        jwtResponse.setId(userId);
        jwtResponse.setUsername(user.getUsername());
        //обновляем аксеес и рефреш токен
        jwtResponse.setAccessToken(
                createAccessToken(userId, user.getUsername(), user.getRoles())
        );
        jwtResponse.setRefreshToken(
                createRefreshToken(userId, user.getUsername())
        );
        return jwtResponse;
    }
// методы которые (проверяют) валидируют токен
    // тип возвращаемого значения булеан и принимает в параметрах токен и может проверять любые токены
    // как рефреш так и аксесс токен
    public boolean isValid(
            final String token
    ) {
        // тут делаем обрабный процесс билду токена, то есть начинаем его парсить
        //сохдаем клаймс
        //проверка вся будет состоять в сравнении даты токена (когда за экспайрился,
        // то ест соедующая дата обновления) и нынешнюю дату
        //если дата (слудующего обновления)  позже, чем дата сейчас,то он валидный
        //если наоборот,то он устарел
        Jws<Claims> claims = Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);

        return claims.getPayload()
                .getExpiration()
                .after(new Date());
    }
//метод который достает айди
    private String getId(
            final String token
    ) {
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("id", String.class);
    }
//достаем юзернейм
    private String getUsername(
            final String token
    ) {
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
//теперь пиешм метод возваращаемый аутентификацию
    //то есть наш jwttokenprovider должен спрингу сказать в определенный момент,что вот он прочитал токены
    //и я понял,что там такой то юзер и я возвращаю его аутентификацию
    //метод по токену возвращает аутентификацию
    public Authentication getAuthentication(
            final String token
    ) {
        //берем юзер нейм из токена(это все парсится в предыдущем методе
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(
                username
        );
        ///UsernamePasswordAuthenticationToken это тоже класс спригн секьюрити, который принимает на вход
        //Принципал — это текущий вошедший в систему пользователь. Однако вы извлекаете его через контекст
        // безопасности, который привязан к текущему потоку, и, как таковой, он также привязан к
        // текущему запросу и его сеансу.
        //SecurityContextHolder.getContext()внутренне получает текущую SecurityContextреализацию через ThreadLocalпеременную. Поскольку запрос привязан к одному потоку, это даст вам контекст текущего запроса .
        //credential в переводе полномочая

        return new UsernamePasswordAuthenticationToken(
                userDetails,//Object principal
                "",//Object credentials
                userDetails.getAuthorities()//Collection<? extends GrantedAuthority> authorities
        );
    }

}