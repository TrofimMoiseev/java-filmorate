package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    private FilmController filmController;
    private Film film;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);
        film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Тестовое описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("Логин");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.create(user);

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

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.create(invalidFilm));

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

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.create(invalidFilm));

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

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.create(invalidFilm));

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

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                filmController.update(nonExistentFilm));

        assertEquals("Фильм с id = 999 не найден", exception.getMessage());
    }

    @Test
    void getPopularFilms() {
        filmController.create(film);
        Film popularFilm = new Film();
        popularFilm.setName("Популярный фильм");
        popularFilm.setDescription("Описание");
        popularFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        popularFilm.setDuration(90);
        filmController.create(popularFilm);  // Этот фильм будет популярным

        Collection<Film> popularFilms = filmController.findPopular(10L);

        assertNotNull(popularFilms);
        assertTrue(popularFilms.size() >= 1);  // Должен быть хотя бы один фильм
    }

    @Test
    void putLike_shouldAddLikeIfFilmExists() {
        filmController.create(film);
        Long userId = 1L;
        filmController.putLike(film.getId(), userId);

        assertTrue(film.getLikes().contains(userId), "Лайк не был добавлен.");
    }

    @Test
    void putLike_shouldThrowExceptionIfLikeAlreadyExists() {
        filmController.create(film);
        Long userId = 1L;
        filmController.putLike(film.getId(), userId);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                filmController.putLike(film.getId(), userId));

        assertEquals("Фильму с id = " + film.getId() + " лайк уже поставлен.", exception.getMessage());
    }

    @Test
    void putLike_shouldThrowExceptionIfFilmNotFound() {
        Long nonExistentFilmId = 999L;
        Long userId = 1L;

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                filmController.putLike(nonExistentFilmId, userId));

        assertEquals("Фильм с id = " + nonExistentFilmId + " не найден", exception.getMessage());
    }

    // Тест на удаление лайка
    @Test
    void deleteLike_shouldRemoveLikeIfFilmAndLikeExist() {
        filmController.create(film);
        Long userId = 1L;
        filmController.putLike(film.getId(), userId);  // Ставим лайк

        filmController.deleteLike(film.getId(), userId);

        assertFalse(film.getLikes().contains(userId), "Лайк не был удален.");
    }

    @Test
    void deleteLike_shouldThrowExceptionIfLikeNotFound() {
        filmController.create(film);
        Long userId = 1L;

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                filmController.deleteLike(film.getId(), userId));

        assertEquals("Фильму с id = " + film.getId() + " лайк не поставлен.", exception.getMessage());
    }

    @Test
    void deleteLike_shouldThrowExceptionIfFilmNotFound() {
        Long nonExistentFilmId = 999L;  // Несуществующий id фильма
        Long userId = 1L;

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                filmController.deleteLike(nonExistentFilmId, userId));

        assertEquals("Фильм с id = " + nonExistentFilmId + " не найден", exception.getMessage());
    }
}
