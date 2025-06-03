package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;


@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;

    public Film findFilmById(Long id) {
        log.info("Обработка GET-запроса на получение фильма по айди.");
        return filmStorage.findFilmById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id = {} не найден", id);
                    return new NotFoundException("Фильм с id = " + id + " не найден");
                });
    }

    public Collection<Film> findAll() {
        log.info("Обработка GET-запроса на получение всех фильмов.");
        return filmStorage.findAll();
    }

    public Collection<Film> findPopular(int count, Integer genreId, Integer year) {
        return filmStorage.findPopular(count, genreId, year);
    }

    public Collection<Film> findFilmsByDirectorId(Long id, String sortBy) {
        log.info("Обработка GET-запроса на получение фильмов по айди режиссера.");
        if (directorStorage.findDirectorById(id).isEmpty()) {
            log.warn("Режиссер с id = {} не найден", id);
            throw new NotFoundException("Режиссер с id = " + id + " не найден");
        }
        return filmStorage.findFilmsByDirectorId(id, sortBy);
    }

    public Film create(Film film) {
        log.info("Обработка POST-запроса на добавление фильма: {}", film);
        check(film);
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        log.info("Обработка PUT-запрос на обновление фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("Обновление отклонено — ID не указан");
            throw new ConditionsNotMetException("Id не указан");
        }

        Film film = filmStorage.findFilmById(newFilm.getId())
                .orElseThrow(() -> {
                    log.warn("Фильм с id = {} не найден", newFilm.getId());
                    return new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
                });

        if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
            film.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
            film.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null &&
                !newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            film.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null && newFilm.getDuration() > 0) {
            film.setDuration(newFilm.getDuration());
        }

        if (newFilm.getMpa() != null) {
            film.setMpa(newFilm.getMpa());
        }

        if (newFilm.getGenres() != null) {
            film.setGenres(newFilm.getGenres());
        }

        if (newFilm.getDirectors() != null) {
            film.setDirectors(newFilm.getDirectors());
        }
        return filmStorage.update(film);
    }

    public void putLike(Long filmId, Long userId) {
        if (!filmStorage.checkId(filmId)) {
            log.warn("Фильм с id = {}, не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }

        if (!userStorage.checkId(userId)) {
            log.warn("Пользователь с id = {}, не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        log.info("Лайк поставлен фильму с id {}.", filmId);
        filmStorage.putLike(userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        if (!filmStorage.checkId(filmId)) {
            log.warn("Фильм с id = {}, не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }

        if (!userStorage.checkId(userId)) {
            log.warn("Пользователь с id = {}, не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        log.info("Лайк фильму с id {}, удален.", filmId);
        filmStorage.deleteLike(userId, filmId);
    }

    public Collection<Film> findCommonFilms(Long userId, Long friendId) {
        log.info("Обработка GET-запроса на получение общих фильмов по айди двух пользователей.");
        return filmStorage.findCommonFilms(userId, friendId);
    }

    public Collection<Film> findFilmsDirectorsByQuery(String query, String by) {
        log.info("Обработка GET-запроса на поиск фильмов, режиссеров по ключевому слову.");
        return filmStorage.findFilmsDirectorsByQuery(query, by);
    }

    public void deleteFilm(Long filmId) {
        log.info("Обработка DELETE-запрос на удаление фильма");
        filmStorage.deleteFilm(filmId);
    }

    private void check(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена — имя фильма отсутствует");
            throw new ValidationException("Имя указано неверно");
        } else if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена — описание превышает 200 символов");
            throw new ValidationException("Количество символов превышает допустимое количество");
        } else if (film.getReleaseDate() == null ||
                film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.warn("Валидация не пройдена — слишком старая дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза указана неверно");
        } else if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Валидация не пройдена — неверная продолжительность: {}", film.getDuration());
            throw new ValidationException("Продолжительность указана неверно");
        }
    }
}
