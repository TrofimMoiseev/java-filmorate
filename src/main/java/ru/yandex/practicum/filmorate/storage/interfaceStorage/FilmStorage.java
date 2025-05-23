package ru.yandex.practicum.filmorate.storage.interfaceStorage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Optional<Film> findFilmById(Long id);

    Collection<Film> findAll();

    Collection<Film> findPopular(int count);

    Film create(Film film);

    Film update(Film film);

    boolean checkId(Long id);

    void putLike(Long userId, Long filmId);

    void deleteLike(Long userId, Long filmId);
}
