package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    String error;
    String details;

    public ErrorResponse(String error, String details) {
        this.error = error;
        this.details = details;
    }
}
