package ru.yandex.practicum.filmorate.storage.interfacestorage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    Optional<Director> findDirectorById(Long id);

    Collection<Director> findAll();

    Director create(Director director);

    Director update(Director director);

    boolean checkId(Long id);

    void deleteDirector(Long id);
}
