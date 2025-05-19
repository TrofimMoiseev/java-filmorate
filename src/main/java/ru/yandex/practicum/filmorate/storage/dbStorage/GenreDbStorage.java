
package ru.yandex.practicum.filmorate.storage.dbStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.genre.GenreRepository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.GenreStorage;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final GenreRepository genreRepository;

    public Collection<Genre> findAll() {
        return genreRepository.findAllGenres();
    }

    public Genre findById(Long id) {
        log.info("Ищем жанр в хранилище по id {}", id);
        return genreRepository.findGenreById(id);

    }
}
