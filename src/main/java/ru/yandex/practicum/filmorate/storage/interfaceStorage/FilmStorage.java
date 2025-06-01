package ru.yandex.practicum.filmorate.storage.interfaceStorage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Optional<Film> findFilmById(Long id);

    Collection<Film> findAll();

    Collection<Film> findPopular(int count);

    Collection<Film> findFilmsByDirectorId(Long id, String sortBy);

    Film create(Film film);

    Film update(Film film);

    boolean checkId(Long id);

    void putLike(Long userId, Long filmId);

    void deleteLike(Long userId, Long filmId);

    Collection<Film> findCommonFilms(Long userId, Long friendId);

    Collection<Film> findFilmsDirectorsByQuery(String query, String by);

    void deleteFilm(Long filmId);
}
