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
    public ExceptionBody handleConstraintViolation(
            final ConstraintViolationException e
    ) {
        ExceptionBody exceptionBody = new ExceptionBody("Validation failed.");
        exceptionBody.setErrors(e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
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