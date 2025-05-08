package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController { //работа с запросами

    FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public Film findFilmById(
            @PathVariable Long id
    ) {
        log.info("Получен GET-запрос на получение фильма по айди.");
        return filmService.findFilmById(id);
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен GET-запрос на получение всех фильмов.");
        return filmService.findAll();
    }

    @GetMapping("/popular")
    public Collection<Film> findPopular(@RequestParam(required = false, defaultValue = "10") Long count) {
        log.info("Получен GET-запрос на получение популярных фильмов.");
        return filmService.findPopular(count);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен POST-запрос на добавление фильма: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Получен PUT-запрос на обновление фильма: {}", film);
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putLike(
            @PathVariable("id") Long filmId,
            @PathVariable Long userId
    ) {
        log.info("Получен PUT-запрос на постановку лайка");
        filmService.putLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLike(
            @PathVariable("id") Long filmId,
            @PathVariable Long userId
    ) {
        log.info("Получен Delete-запрос на удаление лайка");
        filmService.deleteLike(filmId, userId);
    }
}
