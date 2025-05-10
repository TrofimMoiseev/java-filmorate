package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {

    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userService);
        user = new User();
        user.setEmail("test@example.com");
        user.setLogin("Логин");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void returnAllUsers() {
        Collection<User> emptyList = userController.findAll();
        assertNotNull(emptyList);
        assertEquals(0, emptyList.size());

        User addedUser = userController.create(user);
        Collection<User> allUsers = userController.findAll();

        assertEquals(1, allUsers.size());
        assertTrue(allUsers.contains(addedUser));
    }

    @Test
    void createUser() {
        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertEquals(user.getLogin(), createdUser.getLogin());
        assertEquals(user.getName(), createdUser.getName());
    }

    @Test
    void updateUser() {
        User existingUser = userController.create(user);
        existingUser.setName("Обновленный Пользователь");

        User updatedUser = userController.update(existingUser);

        assertEquals("Обновленный Пользователь", updatedUser.getName());
        assertEquals(user.getEmail(), updatedUser.getEmail());
    }

    @Test
    void throwValidationExceptionForInvalidEmail() {
        User invalidUser = new User();
        invalidUser.setEmail("invalidemail");  // Некорректный email
        invalidUser.setLogin("Логин");
        invalidUser.setName("Пользователь");
        invalidUser.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () ->
            userController.create(invalidUser));

        assertEquals("Имейл указан неверно", exception.getMessage());
    }

    @Test
    void throwValidationExceptionForEmailAlreadyUsed() {
        User firstUser = new User();
        firstUser.setEmail("used@example.com");
        firstUser.setLogin("логин1");
        firstUser.setName("Пользователь 1");
        firstUser.setBirthday(LocalDate.of(2000, 1, 1));

        userController.create(firstUser);

        User secondUser = new User();
        secondUser.setEmail("used@example.com");
        secondUser.setLogin("логин2");
        secondUser.setName("Пользователь 2");
        secondUser.setBirthday(LocalDate.of(2001, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () ->
            userController.create(secondUser));

        assertEquals("Этот имейл уже использутся", exception.getMessage());
    }

    @Test
    void throwValidationExceptionForEmptyLogin() {
        User invalidUser = new User();
        invalidUser.setEmail("valid@example.com");
        invalidUser.setLogin("");  // Некорректный логин
        invalidUser.setName("Корректное Имя");
        invalidUser.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () ->
            userController.create(invalidUser));

        assertEquals("Логин указан неверно", exception.getMessage());
    }

    @Test
    void throwValidationExceptionForFutureBirthday() {
        User invalidUser = new User();
        invalidUser.setEmail("valid@example.com");
        invalidUser.setLogin("Логин");
        invalidUser.setName("Пользователь");
        invalidUser.setBirthday(LocalDate.of(2100, 1, 1));  // Некорректная дата рождения

        ValidationException exception = assertThrows(ValidationException.class, () ->
            userController.create(invalidUser));

        assertEquals("Дата рождения указана неверно", exception.getMessage());
    }

    @Test
    void throwConditionsNotMetExceptionWhenUpdatingNonExistentUser() {
        User nonExistentUser = new User();
        nonExistentUser.setId(999L);
        nonExistentUser.setEmail("nonexistent@example.com");
        nonExistentUser.setLogin("Логин");
        nonExistentUser.setName("Пользователь Не Существует");
        nonExistentUser.setBirthday(LocalDate.of(2000, 1, 1));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            userController.update(nonExistentUser));

        assertEquals("Пользователь с id = 999 не найден", exception.getMessage());
    }

    @Test
    void throwConditionsNotMetExceptionWhenIdNotProvidedForUpdate() {
        User userToUpdate = new User();
        userToUpdate.setEmail("valid@example.com");
        userToUpdate.setLogin("Логин");
        userToUpdate.setName("Пользователь");
        userToUpdate.setBirthday(LocalDate.of(2000, 1, 1));

        ConditionsNotMetException exception = assertThrows(ConditionsNotMetException.class, () ->
                userController.update(userToUpdate));  // Нет ID для обновления

        assertEquals("Id не указан", exception.getMessage());
    }

    @Test
    void addFriend() {
        User user1 = new User();
        user1.setEmail("friend1@example.com");
        user1.setLogin("friend1");
        user1.setName("Friend 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("friend2@example.com");
        user2.setLogin("friend2");
        user2.setName("Friend 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        userController.create(user1);
        userController.create(user2);

        userController.putFriend(user1.getId(), user2.getId());

        assertTrue(userController.getFriends(user1.getId()).contains(user2));
        assertTrue(userController.getFriends(user2.getId()).contains(user1));
    }

    @Test
    void removeFriend() {
        User user1 = new User();
        user1.setEmail("friend1@example.com");
        user1.setLogin("friend1");
        user1.setName("Friend 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("friend2@example.com");
        user2.setLogin("friend2");
        user2.setName("Friend 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        userController.create(user1);
        userController.create(user2);

        userController.putFriend(user1.getId(), user2.getId());

        assertTrue(userController.getFriends(user1.getId()).contains(user2));

        userController.deleteFriend(user1.getId(), user2.getId());

        assertFalse(userController.getFriends(user1.getId()).contains(user2));
    }

    @Test
    void getFriendsList() {
        User user1 = new User();
        user1.setEmail("friend1@example.com");
        user1.setLogin("friend1");
        user1.setName("Friend 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("friend2@example.com");
        user2.setLogin("friend2");
        user2.setName("Friend 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        userController.create(user1);
        userController.create(user2);

        userController.putFriend(user1.getId(), user2.getId());

        Collection<User> friends = userController.getFriends(user1.getId());

        assertTrue(friends.contains(user2));
    }

    @Test
    void getCommonFriends() {
        User user1 = new User();
        user1.setEmail("friend1@example.com");
        user1.setLogin("friend1");
        user1.setName("Friend 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("friend2@example.com");
        user2.setLogin("friend2");
        user2.setName("Friend 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        User user3 = new User();
        user3.setEmail("friend3@example.com");
        user3.setLogin("friend3");
        user3.setName("Friend 3");
        user3.setBirthday(LocalDate.of(1992, 1, 1));

        userController.create(user1);
        userController.create(user2);
        userController.create(user3);

        userController.putFriend(user1.getId(), user2.getId());
        userController.putFriend(user1.getId(), user3.getId());
        userController.putFriend(user2.getId(), user3.getId());

        Collection<User> commonFriends = userController.getCommonFriends(user1.getId(), user2.getId());

        assertTrue(commonFriends.contains(user3));
    }

    @Test
    void throwConditionsNotMetExceptionWhenUserNotFoundForDeleteFriend() {
        User user1 = new User();
        user1.setEmail("friend1@example.com");
        user1.setLogin("friend1");
        user1.setName("Friend 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        userController.create(user1);

        User nonExistentUser = new User();
        nonExistentUser.setId(999L);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                userController.deleteFriend(user1.getId(), nonExistentUser.getId()));

        assertEquals("Пользователь с id = 999 не найден", exception.getMessage());
    }
}
