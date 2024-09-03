package com.pilot.pilot.web.controller;


import com.pilot.pilot.domain.exception.AccessDeniedException;
import com.pilot.pilot.domain.exception.ExceptionBody;
import com.pilot.pilot.domain.exception.ImageUploadException;
import com.pilot.pilot.domain.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;
// Данный класс перехватывает эксепшн и передает нам в более удобном виде
///Аннотация @RestControllerAdvice позволяет ннам перехватывать исключения ив возвращать объекты
//так же что бы удобней было взаимодействовть создадим класс эксепшн боди
@RestControllerAdvice
public class ControllerAdvice {
//здесь создаем первый ExceptionHandler, который будет приинимать ResourceNotFoundException.class

    //@ExceptionHandler позволяет обрабатывать исключения на уровне отдельного контроллера. Для этого достаточно
    // объявить метод в контроллере, в котором будет содержаться вся логика обработки нужного исключения, и пометить его аннотацией.
    @ExceptionHandler(ResourceNotFoundException.class)
//@ResponseStatus — просто выбрасываем исключение и передаём нужный статус-код.
// Конечно тут возвращаемся к проблеме отсутствия тела сообщения, но в простых случаях такой подход может быть удобен.
    @ResponseStatus(HttpStatus.NOT_FOUND)
    //создаем метод handleResourceNotFound должен вернуть объект типа ExceptionBody
    public ExceptionBody handleResourceNotFound(
// передаем ошибки, которые у нас есть
         final ResourceNotFoundException e
    ) {
        return new ExceptionBody(e.getMessage());
    }
//и возвращаем сообщение из резултата

    //тоже самое
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionBody handleIllegalState(
            final IllegalStateException e
    ) {
        return new ExceptionBody(e.getMessage());
    }

    @ExceptionHandler({
            AccessDeniedException.class,
            org.springframework.security.access.AccessDeniedException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionBody handleAccessDenied() {
        return new ExceptionBody("Access denied.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionBody handleMethodArgumentNotValid(
            final MethodArgumentNotValidException e
    ) {
        ExceptionBody exceptionBody = new ExceptionBody("Validation failed.");
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        exceptionBody.setErrors(errors.stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existingMessage, newMessage) ->
                                existingMessage + " " + newMessage)
                ));
        return exceptionBody;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    //Даннный класс у нас ответчает за формирование текста в консоле по ошибке тела я так думиаю запроса на валидацию
    public ExceptionBody handleConstraintViolation(
        //в аргумент принимает ConstraintViolationException и записываает ее в е 
            final ConstraintViolationException e
    ) {
        //Далее мы создали объект ошибкаи  и передали в аргмуент  текст Validation failed
        ExceptionBody exceptionBody = new ExceptionBody("Validation failed.");
        //туда же пишем (передаем) все ошибки с помощью setErrors и в аргумент передаем из e полученные с помощью getConstraintViolations() ошибки. Далее создаем из этих данных поток с помощью stream()
        //Теперь с данным в потоке мы можем вертеть крутить с помощью методов потока.
    //с помощью конвеерных методов можем сортировать, фильтровать, сравнивать данные, но данные оставются в потоке и что вы вернуть данные в понятном состоянии и сделать лист, нам нужны терминальные методы
        exceptionBody.setErrors(e.getConstraintViolations().stream()//важно что без точки с запятой
                                //терминальныей метод его применяем к результату внутри
                                //метод collect(). Он используется для того, чтобы перейти от потоков к привычным коллекциям — List<T>, Set<T>, Map<T, R> 
                               // toMap() Объект, который преобразует поток в мэп — Map<K, V>
                .collect(Collectors.toMap(
                    ///violation произвольное название элементов внутри, сами пишем. и применяем методы getPropertyPath(). дать путь место ошибки и все это в строке подать toString(),
                        violation -> violation.getPropertyPath().toString(),
                    //забираем сообщение в ошибке 
                        ConstraintViolation::getMessage
                )));
        return exceptionBody;
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionBody handleAuthentication(
            final AuthenticationException e
    ) {
        return new ExceptionBody("Authentication failed.");
    }

    @ExceptionHandler(ImageUploadException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionBody handleImageUpload(
            final ImageUploadException e
    ) {
        return new ExceptionBody(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionBody handleException(
            final Exception e
    ) {
        e.printStackTrace();
        return new ExceptionBody("Internal error.");
    }

}
