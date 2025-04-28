package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен GET-запрос на получение всех пользователей. Всего: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User newUser) {
        log.info("Получен POST-запрос на создание пользователя: {}", newUser);
        check(newUser);

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.debug("Имя пользователя не указано. Используем логин: {}", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }

        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь успешно создан с ID = {}", newUser.getId());
        return newUser;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Получен PUT-запрос на обновление пользователя: {}", newUser);

        if (newUser.getId() == null) {
            log.warn("Обновление отклонено — ID не указан");
            throw new ConditionsNotMetException("Id не указан");
        } else if (!users.containsKey(newUser.getId())) {
            log.warn("Обновление отклонено — пользователь с ID = {} не найден", newUser.getId());
            throw new ConditionsNotMetException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        User oldUser = users.get(newUser.getId());

        if (newUser.getEmail() != null && !newUser.getEmail().isBlank() && !newUser.getEmail().equals(oldUser.getEmail())) {
            if (users.values().stream().anyMatch(user -> user.equals(newUser))) {
                log.warn("Обновление отклонено — email {} уже используется", newUser.getEmail());
                throw new ConditionsNotMetException("Этот имейл уже использутся");
            }
            oldUser.setEmail(newUser.getEmail());
        }

        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            oldUser.setLogin(newUser.getLogin());
        }

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        }

        if (newUser.getBirthday() != null && !newUser.getBirthday().isAfter(LocalDate.now())) {
            oldUser.setBirthday(newUser.getBirthday());
        }

        log.info("Пользователь с ID = {} успешно обновлён", newUser.getId());
        return oldUser;
    }

    private void check(User newUser) {
        if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
            log.warn("Валидация не пройдена — некорректный email: {}", newUser.getEmail());
            throw new ValidationException("Имейл указан неверно");
        } else if (users.values().stream().anyMatch(user -> user.equals(newUser))) {
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

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}