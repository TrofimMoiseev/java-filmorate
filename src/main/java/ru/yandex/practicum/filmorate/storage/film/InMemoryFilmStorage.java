package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage { //логика хранения
    private final Map<Long, Film> films = new HashMap<>();
    private Long sequence = 0L;

    private Long getSequence() {
        return ++sequence;
    }

    @Override
    public Film findFilmById(Long id) {
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
        films.put(film.getId(), film);
        log.info("Фильм с ID = {} обновлён", film.getId());
        return film;
    }

    public boolean checkId(Long id) {
        return films.containsKey(id);
    }
}
