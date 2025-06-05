package ru.yandex.practicum.filmorate.storage.interfacestorage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Optional<Film> findFilmById(Long id);

    Collection<Film> findAll();

    Collection<Film> findPopular(int count, Integer genreId, Integer year);

    Collection<Film> findFilmsByDirectorId(Long id, String sortBy);

    Film create(Film film);

    Film update(Film film);

    boolean checkId(Long id);

    void putLike(Long userId, Long filmId);

    void deleteLike(Long userId, Long filmId);

    Collection<Film> findCommonFilms(Long userId, Long friendId);

    List<Film> findRecommendationsByUser(Long similarUserId, Long userId);

    Collection<Film> findFilmsDirectorsByQuery(String query, String by);

    void deleteFilm(Long filmId);
}
