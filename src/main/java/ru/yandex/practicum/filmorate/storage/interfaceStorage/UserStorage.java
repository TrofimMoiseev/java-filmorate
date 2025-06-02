package ru.yandex.practicum.filmorate.storage.interfaceStorage;

import ru.yandex.practicum.filmorate.DTO.FeedDTO;
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

    Collection<FeedDTO> getFeeds(Long id);

    List<User> findSimilarUsers(Long userId);

    void deleteUser(Long userId);

}
