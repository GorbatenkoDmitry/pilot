package com.pilot.pilot.web.mappers;

import ch.qos.logback.core.model.ComponentModel;
import com.pilot.pilot.domain.user.User;
import com.pilot.pilot.web.dto.user.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserDto dto);
}