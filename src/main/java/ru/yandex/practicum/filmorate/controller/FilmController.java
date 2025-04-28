package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен GET-запрос на получение всех фильмов. Текущее количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film newFilm) {
        log.info("Получен POST-запрос на добавление фильма: {}", newFilm);
        check(newFilm);
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм успешно добавлен с ID = {}", newFilm.getId());
        return newFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Получен PUT-запрос на обновление фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("Обновление отклонено — ID не указан");
            throw new ConditionsNotMetException("Id не указан");
        } else if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
                oldFilm.setName(newFilm.getName());
            }

            if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getReleaseDate() != null &&
                    !newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28)) &&
                    !newFilm.getReleaseDate().isAfter(LocalDate.now())) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            if (newFilm.getDuration() != null && newFilm.getDuration() > 0) {
                oldFilm.setDuration(newFilm.getDuration());
            }

            log.info("Фильм с ID = {} обновлён", newFilm.getId());
            return oldFilm;
    }

    private void check(Film newFilm) {
        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            log.warn("Валидация не пройдена — имя фильма отсутствует");
            throw new ValidationException("Имя указано неверно");
        } else if (newFilm.getDescription() == null || newFilm.getDescription().length() > 200) {
            log.warn("Валидация не пройдена — описание превышает 200 символов");
            throw new ValidationException("Количество символов превышает допустимого");
        } else if (newFilm.getReleaseDate() == null ||
                newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28)) ||
                newFilm.getReleaseDate().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена — слишком старая дата релиза: {}", newFilm.getReleaseDate());
            throw new ValidationException("Дата релиза указана неверно");
        } else if (newFilm.getDuration() == null || newFilm.getDuration() <= 0) {
            log.warn("Валидация не пройдена — неверная продолжительность: {}", newFilm.getDuration());
            throw new ValidationException("Продолжительность указана неверно");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
