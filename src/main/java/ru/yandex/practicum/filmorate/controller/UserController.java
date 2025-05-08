package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable Long id) {
        log.info("Получен GET-запроса на получение пользователя по id {}.", id);
        return userService.findUserById(id);
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен GET-запрос на получение всех пользователей.");
        return userService.findAll();
    }

    @GetMapping("/{id}/friends")
    Collection<User> getFriends(@PathVariable Long id) {
        log.info("Получен GET-запрос на получение всех друзей пользователя {}.", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    Collection<User> getCommonFriends(
            @PathVariable("id") Long userId,
            @PathVariable Long otherId
    ) {
        log.info("Получен GET-запрос на получение общих друзей пользователя {} с пользователем {}.", userId, otherId);
        return userService.getCommonFriends(userId, otherId);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Получен POST-запрос на создание пользователя: {}", user);
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Получен PUT-запрос на обновление пользователя: {}", user);
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putFriend(
            @PathVariable("id") Long userId,
            @PathVariable Long friendId
    ) {
        log.info("Получен PUT-запрос на добавления в друзья");
        userService.putFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFriend(
            @PathVariable("id") Long userId,
            @PathVariable Long friendId
    ) {
        log.info("Получен Delete-запрос на удаление друга");
        userService.deleteFriend(userId, friendId);
    }
}