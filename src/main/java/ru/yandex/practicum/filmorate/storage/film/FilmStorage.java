package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film findFilmById(Long id);

    Collection<Film> findAll();

    Collection<Film> findPopular(Long count);

    Film create(Film film);

    Film update(Film film);

    boolean checkId(Long id);
}
