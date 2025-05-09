package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User findUserById(Long id);

    Collection<User> findAll();

    User create(User user);

    User update(User user);

    boolean checkId(Long id);
}
