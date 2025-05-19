package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Like {
    Long userId;
    Long filmId;
}
