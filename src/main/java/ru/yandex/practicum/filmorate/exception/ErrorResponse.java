package ru.yandex.practicum.filmorate.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse { //предлагает класс record сделать нужно ли?
    private final String error;
    private final String details;
}
