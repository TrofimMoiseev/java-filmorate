package ru.yandex.practicum.filmorate.storage.interfaceStorage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Optional<User> findUserById(Long id);

    Collection<User> findAll();

    User create(User user);

    User update(User user);

    boolean checkId(Long id);

    void putFriend(Long userId, Long friendId);

    Collection<User> getFriends(Long userId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> getCommonFriends(Long userId, Long friendId);

    void deleteUser(Long userId);

    Collection<Film> getRecommendations(Long userId);
}
