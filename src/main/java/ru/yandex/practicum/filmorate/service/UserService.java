package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.DTO.FeedDTO;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User findUserById(Long id) {
        log.info("Обработка GET-запроса на получение пользователя по id.");

        return userStorage.findUserById(id)
                .orElseThrow(() -> {
                        log.warn("Пользователь с id = {} не найден", id);
        return new NotFoundException("Пользователь с id = " + id + " не найден");
                });
    }

    public Collection<User> findAll() {
        log.info("Получен GET-запрос на получение всех пользователей.");
        return userStorage.findAll();
    }

    public Collection<User> getFriends(Long id) {
        if (!userStorage.checkId(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        log.info("Список предоставлен.");
        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        if (!userStorage.checkId(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!userStorage.checkId(otherId)) {
            throw new NotFoundException("Пользователь с id = " + otherId + " не найден");
        }

        log.info("Общие друзья пользователей с id = {} и id = {}: {}", userId, otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    public User create(User user) {
        log.info("Получен POST-запрос на создание пользователя: {}", user);
        check(user);

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано. Используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        return userStorage.create(user);
    }

    public User update(User newUser) {
        log.info("Получен PUT-запрос на обновление пользователя: {}", newUser);

        if (newUser.getId() == null) {
            log.warn("Обновление отклонено — ID не указан");
            throw new ConditionsNotMetException("Id не указан");
        }

        User user = userStorage.findUserById(newUser.getId())
                .orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден", newUser.getId());
                    return new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
                });

        if (newUser.getEmail() != null && !newUser.getEmail().isBlank() && !newUser.getEmail().equals(user.getEmail())) {
            if (userStorage.findAll().stream().anyMatch(anyUser -> anyUser.getEmail().equals(newUser.getEmail()))) {
                log.warn("Обновление отклонено — email {} уже используется", newUser.getEmail());
                throw new ConditionsNotMetException("Этот имейл уже использутся");
            }
            user.setEmail(newUser.getEmail());
        }

        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            user.setLogin(newUser.getLogin());
        }

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            user.setName(newUser.getName());
        }

        if (newUser.getBirthday() != null && !newUser.getBirthday().isAfter(LocalDate.now())) {
            user.setBirthday(newUser.getBirthday());
        }
        return userStorage.update(user);
    }

    public void putFriend(Long userId, Long friendId) {
        if (!userStorage.checkId(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!userStorage.checkId(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        userStorage.putFriend(userId, friendId);
        log.info("Пользователь с ID = {} добавил в друзья пользователя с ID = {}", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (!userStorage.checkId(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!userStorage.checkId(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователь с ID = {} удалил из друзей пользователя с ID = {}", userId, friendId);
    }

    public void deleteUser(Long userId) {
        log.info("Получен Delete-запрос на удаление пользователя");
        if (!userStorage.checkId(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        userStorage.deleteUser(userId);
    }

    public Collection<Film> getRecommendations(Long userId) {
        if (!userStorage.checkId(userId)) {
            log.warn("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return userStorage.findRecommendedFilmsForUser(userId);
    }

    public Collection<FeedDTO> getFeeds(Long id) {
        if (!userStorage.checkId(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return userStorage.getFeeds(id);
    }

    private void check(User newUser) {
        if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
            log.warn("Валидация не пройдена — некорректный email: {}", newUser.getEmail());
            throw new ValidationException("Имейл указан неверно");
        } else if (userStorage.findAll().stream().anyMatch(user -> user.getEmail().equals(newUser.getEmail()))) {
            log.warn("Валидация не пройдена — email уже используется: {}", newUser.getEmail());
            throw new ValidationException("Этот имейл уже использутся");
        } else if (newUser.getLogin() == null || newUser.getLogin().isBlank()) {
            log.warn("Валидация не пройдена — логин отсутствует");
            throw new ValidationException("Логин указан неверно");
        } else if (newUser.getBirthday() == null || newUser.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена — дата рождения в будущем: {}", newUser.getBirthday());
            throw new ValidationException("Дата рождения указана неверно");
        }
    }
}
