
package ru.yandex.practicum.filmorate.storage.dbStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.film.FilmRepository;
import ru.yandex.practicum.filmorate.dal.like.LikeRepository;
import ru.yandex.practicum.filmorate.dal.user.UserRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.FilmStorage;

import java.util.Collection;

@Slf4j
@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;


    @Override
    public Collection<Film> findAll() {
        return filmRepository.findAllFilms();
    }

    @Override
    public Film findFilmById(Long filmId) {
        log.debug("Поиск фильма в хранилище");
        return filmRepository.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }

    @Override
    public Film create(Film newFilm) {
        log.info("Добавление фильма в хранилище");
        return filmRepository.save(newFilm);
    }

    @Override
    public Film update(Film updFilm) {
        log.debug("Обновление фильма в хранилище");
        return filmRepository.updateFilm(updFilm);
    }

    @Override
    public boolean checkId(Long id) {
        return filmRepository.checkId(id);
    }

    @Override
    public boolean putLike(Long userId, Long filmId) {
        log.debug("Добавление лайка в хранилище");
        return likeRepository.putLike(userId, filmId);
    }

    @Override
    public boolean deleteLike(Long userId, Long filmId) {
        log.debug("Удаление лайка в хранилище");
        return likeRepository.deleteLike(userId, filmId);
    }

    @Override
    public int countLikes(Long filmId) {
        log.debug("Запрос кол-ва лайков в хранилище");
        return likeRepository.getLikeCount(filmId);
    }

    @Override
    public Collection<Film> findPopular(int count) {
        log.debug("Запрос популярных фильмов в хранилище");
        return likeRepository.findPopularFilms(count);
    }
}
