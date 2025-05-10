package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User findUserById(Long id) {
        log.info("Обработка GET-запроса на получение пользователя по id.");
        if (!userStorage.checkId(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return userStorage.findUserById(id);
    }

    public Collection<User> findAll() {
        log.info("Получен GET-запрос на получение всех пользователей.");
        return userStorage.findAll();
    }

    public Collection<User> getFriends(Long id) {
        if (!userStorage.checkId(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        Set<Long> friendIds = userStorage.findUserById(id).getFriends();

        Collection<User> friends = friendIds.stream()
                .map(userStorage::findUserById)
                .collect(Collectors.toList());

        log.info("Список предоставлен. Текущее количество друзей {}.", friends.size());
        return friends;
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        if (!userStorage.checkId(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!userStorage.checkId(otherId)) {
            throw new NotFoundException("Пользователь с id = " + otherId + " не найден");
        }

        Set<Long> userFriends = userStorage.findUserById(userId).getFriends();
        Set<Long> otherUserFriends = userStorage.findUserById(otherId).getFriends();

        Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherUserFriends);

        Collection<User> commonFriends = commonFriendIds.stream()
                .map(userStorage::findUserById)
                .collect(Collectors.toList());

        log.info("Общие друзья пользователей с id = {} и id = {}: {}", userId, otherId, commonFriends.size());
        return commonFriends;
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

        if (!userStorage.checkId(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        User user = userStorage.findUserById(newUser.getId());

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
        return userStorage.update(newUser);
    }

    public void putFriend(Long userId, Long friendId) {
        if (!userStorage.checkId(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!userStorage.checkId(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь с ID = {} добавил в друзья пользователя с ID = {}", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (!userStorage.checkId(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!userStorage.checkId(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь с ID = {} удалил из друзей пользователя с ID = {}", userId, friendId);
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
