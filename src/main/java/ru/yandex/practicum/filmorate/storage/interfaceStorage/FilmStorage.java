package ru.yandex.practicum.filmorate.storage.interfaceStorage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film findFilmById(Long id);

    Collection<Film> findAll();

    Collection<Film> findPopular(int count);

    Film create(Film film);

    Film update(Film film);

    boolean checkId(Long id);

    boolean putLike(Long userId, Long filmId);

    boolean deleteLike(Long userId, Long filmId);

    int countLikes(Long filmId);
}
