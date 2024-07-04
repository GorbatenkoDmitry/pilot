package com.pilot.pilot.web.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pilot.pilot.domain.task.Status;
import com.pilot.pilot.web.dto.validator.OnCreate;
import com.pilot.pilot.web.dto.validator.OnUpdate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
@Data
public class TaskDto {
    @NotNull(message = "Id must be not null", groups = OnUpdate.class)
    private Long id;
    @NotNull(message = "TItle must be not null", groups = {OnCreate.class, OnUpdate.class})
    @Length(max = 255, message = "Title length must be smaller than 255 symbols", groups = {OnCreate.class, OnUpdate.class})
    private String title;
    @Length(max = 255, message = "Descriotion length must be smaller than 255 symbols", groups = {OnCreate.class, OnUpdate.class})

    private String description;
    private Status status;
    @DateTimeFormat (iso = DateTimeFormat.ISO.TIME)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expirationDate;
}