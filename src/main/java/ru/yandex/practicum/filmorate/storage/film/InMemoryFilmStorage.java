package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage { //логика хранения
    private final Map<Long, Film> films = new HashMap<>();
    private Long sequence = 1L;

    private Long getSequence() {
        return sequence++;
    }

    @Override
    public Film findFilmById(Long id) {
        if (!films.containsKey(id)) {
            log.warn("Фильм с id = {}, не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        log.info("Фильм предоставлен");
        return films.get(id);
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Список предоставлен. Текущее количество: {}", films.size());
        return films.values();
    }

    @Override
    public Collection<Film> findPopular(Long count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Film create(Film film) {
        film.setId(getSequence());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с ID = {}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id = {}, не найден", film.getId());
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        Film oldFilm = films.get(film.getId());

        if (film.getName() != null && !film.getName().isBlank()) {
            oldFilm.setName(film.getName());
        }

        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            oldFilm.setDescription(film.getDescription());
        }

        if (film.getReleaseDate() != null &&
                !film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28)) &&
                !film.getReleaseDate().isAfter(LocalDate.now())) {
            oldFilm.setReleaseDate(film.getReleaseDate());
        }

        if (film.getDuration() != null && film.getDuration() > 0) {
            oldFilm.setDuration(film.getDuration());
        }

        log.info("Фильм с ID = {} обновлён", film.getId());
        return oldFilm;
    }

    @Override
    public void putLike(Long filmId, Long userId) {
        if (!films.containsKey(filmId)) {
            log.warn("Фильм с id = {}, не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        Film film = films.get(filmId);

        if (film.getLikes().contains(userId)) {
            log.warn("Фильм с id = {}, лайк уже поставлен", filmId);
            throw new NotFoundException("Фильму с id = " + filmId + " лайк уже поставлен.");
        }
        log.info("Лайк поставлен фильму с id {}.", filmId);
        films.get(filmId).getLikes().add(userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        if (!films.containsKey(filmId)) {
            log.warn("Фильм с id = {}, не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        Film film = films.get(filmId);
         if (!film.getLikes().contains(userId)) {
             log.warn("Фильм с id = {}, лайк не поставлен", filmId);
             throw new NotFoundException("Фильму с id = " + filmId + " лайк не поставлен.");
         }
        log.info("Лайк фильму с id {}, удален.", filmId);
        film.getLikes().remove(userId);
    }
}
