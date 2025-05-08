package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;


@Slf4j
@Service
public class FilmService { //логика обработки запросов

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film findFilmById(Long id) {
        log.info("Обработка GET-запроса на получение фильма по айди.");
        return filmStorage.findFilmById(id);
    }

    public Collection<Film> findAll() {
        log.info("Обработка GET-запроса на получение всех фильмов.");
        return filmStorage.findAll();
    }

    public Collection<Film> findPopular(Long count) {
        log.info("Обработка GET-запроса на получение популярных фильмов.");
        return filmStorage.findPopular(count);
    }

    public Film create(Film film) {
        log.info("Обработка POST-запроса на добавление фильма: {}", film);
        check(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.info("Обработка PUT-запрос на обновление фильма: {}", film);
        if (film.getId() == null) {
            log.warn("Обновление отклонено — ID не указан");
            throw new ConditionsNotMetException("Id не указан");
        }
        return filmStorage.update(film);
    }

    public void putLike(Long filmId, Long userId) {
        log.info("Обработка PUT-запроса на постановку лайка");
        if (!userStorage.checkId(userId)) {
            log.warn("Пользователь с id = {}, не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        filmStorage.putLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        log.info("Обработка Delete-запроса на удаление лайка");
        filmStorage.deleteLike(filmId, userId);
    }

    private void check(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена — имя фильма отсутствует");
            throw new ValidationException("Имя указано неверно");
        } else if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена — описание превышает 200 символов");
            throw new ValidationException("Количество символов превышает допустимое количество");
        } else if (film.getReleaseDate() == null ||
                film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28)) ||
                film.getReleaseDate().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена — слишком старая дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза указана неверно");
        } else if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Валидация не пройдена — неверная продолжительность: {}", film.getDuration());
            throw new ValidationException("Продолжительность указана неверно");
        }
    }
}
