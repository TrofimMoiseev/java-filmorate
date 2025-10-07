package ru.yandex.practicum.filmorate.storage.interfacestorage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {

    Collection<Genre> findAll();

    Genre findById(Long id);
}
