package ru.yandex.practicum.filmorate.storage.interfaceStorage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User findUserById(Long id);

    Collection<User> findAll();

    User create(User user);

    User update(User user);

    boolean checkId(Long id);

    void putFriend(Long userId, Long friendId);

    Collection<User> getFriends(Long userId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> getCommonFriends(Long userId, Long friendId);
}
