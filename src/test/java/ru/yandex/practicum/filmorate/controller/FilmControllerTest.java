package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    private FilmController filmController;
    private Film film;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        filmController = new FilmController();
        film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Тестовое описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
    }

    @Test
    void returnAllFilms() {
        // Проверка, что список фильмов пуст изначально
        Collection<Film> emptyList = filmController.findAll();
        assertNotNull(emptyList);
        assertEquals(0, emptyList.size());

        // Добавление фильма и проверка, что он появляется в списке
        Film addedFilm = filmController.create(film);
        Collection<Film> allFilms = filmController.findAll();

        assertEquals(1, allFilms.size());
        assertTrue(allFilms.contains(addedFilm));
    }

    @Test
    void createFilm() {
        // Создание фильма и проверка его свойств
        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals(film.getName(), createdFilm.getName());
        assertEquals(film.getDescription(), createdFilm.getDescription());
    }

    @Test
    void updateFilm() {
        // Создание фильма и обновление его имени
        Film existingFilm = filmController.create(film);
        existingFilm.setName("Обновлённый фильм");

        Film updatedFilm = filmController.update(existingFilm);

        assertEquals("Обновлённый фильм", updatedFilm.getName());
        assertEquals(film.getDescription(), updatedFilm.getDescription());
    }

    @Test
    void throwValidationExceptionForInvalidDuration() {
        // Проверка фильма с некорректной продолжительностью
        Film invalidFilm = new Film();
        invalidFilm.setName("Неверный фильм");
        invalidFilm.setDescription("Описание");
        invalidFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidFilm.setDuration(-1);  // Некорректная продолжительность

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.create(invalidFilm);
        });

        assertEquals("Продолжительность указана неверно", exception.getMessage());
    }

    @Test
    void throwValidationExceptionForEmptyName() {
        // Проверка фильма с пустым именем
        Film invalidFilm = new Film();
        invalidFilm.setName("");  // Некорректное имя
        invalidFilm.setDescription("Описание");
        invalidFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidFilm.setDuration(100);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.create(invalidFilm);
        });

        assertEquals("Имя указано неверно", exception.getMessage());
    }

    @Test
    void throwValidationExceptionForInvalidReleaseDate() {
        // Проверка фильма с некорректной датой релиза
        Film invalidFilm = new Film();
        invalidFilm.setName("Корректное имя");
        invalidFilm.setDescription("Описание");
        invalidFilm.setReleaseDate(LocalDate.of(1800, 1, 1));  // Некорректная дата релиза
        invalidFilm.setDuration(100);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.create(invalidFilm);
        });

        assertEquals("Дата релиза указана неверно", exception.getMessage());
    }

    @Test
    void throwNotFoundExceptionWhenUpdatingNonExistentFilm() {
        // Проверка обновления несуществующего фильма
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999L);
        nonExistentFilm.setName("Ненайденный фильм");
        nonExistentFilm.setDescription("Описание");
        nonExistentFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        nonExistentFilm.setDuration(100);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            filmController.update(nonExistentFilm);
        });

        assertEquals("Фильм с id = 999 не найден", exception.getMessage());
    }
}
