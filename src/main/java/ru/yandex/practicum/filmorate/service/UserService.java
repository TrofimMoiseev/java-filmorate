package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
public class UserService {

    UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User findUserById(Long id) {
        log.info("Обработка GET-запроса на получение пользователя по id.");
        return userStorage.findUserById(id);
    }

    public Collection<User> findAll() {
        log.info("Получен GET-запрос на получение всех пользователей.");
        return userStorage.findAll();
    }

    public Collection<User> getFriends(Long id) {
        log.info("Обработка GET-запроса на получение всех друзей пользователя {}.", id);
        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
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

    public User update( User user) {
        log.info("Получен PUT-запрос на обновление пользователя: {}", user);

        if (user.getId() == null) {
            log.warn("Обновление отклонено — ID не указан");
            throw new ConditionsNotMetException("Id не указан");
        }
        return userStorage.update(user);
    }

    public void putFriend(Long userId, Long friendId) {
        log.info("Обработка PUT-запроса на добавление друга");
        userStorage.putFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Обработка Delete-запроса на удаление друга");
        userStorage.deleteFriend(userId, friendId);
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
