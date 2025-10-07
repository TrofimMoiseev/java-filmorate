package ru.yandex.practicum.filmorate.storage.interfacestorage;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
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

    List<Feed> getFeeds(Long id);

    List<User> findSimilarUsers(Long userId);

    List<Film> findRecommendedFilmsForUser(Long userId);

    void deleteUser(Long userId);

}
