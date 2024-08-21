package com.pilot.pilot.domain.exception;
//Данный класс служит для взаимодействия с controllerAdvice
//Этот класс объект и мы его будем возвразать в случае эксепшн

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ExceptionBody {


    //даный класс содежрит толкьо тест ошибки,
    private String message;
    private Map<String, String> errors;

    public ExceptionBody(
            final String message
    ) {
        this.message = message;
    }

}