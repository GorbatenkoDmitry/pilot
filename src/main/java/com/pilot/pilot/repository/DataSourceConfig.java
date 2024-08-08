package com.pilot.pilot.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;

public class DataSourceConfig {

// делаем тег     @Configuration что бы мы могли подключать данный класс из других классов
// @RequiredArgsConstructor то есть конструтор и что бы переменная была заполнена ставим ее файнал
    //теперь если она будет пустая будет ошибка выдаваться
    @Configuration
    @RequiredArgsConstructor
    public class DataSourceConfig {
//создали переменную типа датасерс класс из javax.sql
        private final DataSource dataSource;
//создали метод подключения и кинули туда гет коннектион передав ей переменную с данными dataSource (перевод источник данныхэ)
        public Connection getConnection() {
            return DataSourceUtils.getConnection(dataSource);
        }

    }