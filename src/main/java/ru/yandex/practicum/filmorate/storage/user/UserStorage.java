package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User findUserById(Long id);

    Collection<User> findAll();

    Collection<User> getFriends(Long id);

    Collection<User> getCommonFriends(Long id, Long otherId);

    User create(User user);

    User update(User user);

    void putFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    boolean checkId(Long id);
}
