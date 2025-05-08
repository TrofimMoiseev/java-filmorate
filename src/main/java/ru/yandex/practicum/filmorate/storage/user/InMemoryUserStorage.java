package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long sequence = 0L;

    private Long getSequence() {
        return ++sequence;
    }

    @Override
    public User findUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        log.info("Пользователь предоставлен");
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        log.info("Список предоставлен. Текущее количество пользователей {}.", users.size());
        return users.values();
    }

    @Override
    public Collection<User> getFriends(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        Set<Long> friendIds = users.get(id).getFriends();

        Collection<User> friends = friendIds.stream()
                .map(users::get)  // Получаем объект User по ID
                .collect(Collectors.toList());

        log.info("Список предоставлен. Текущее количество друзей {}.", friends.size());
        return friends;
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!users.containsKey(otherId)) {
            throw new NotFoundException("Пользователь с id = " + otherId + " не найден");
        }

        Set<Long> userFriends = users.get(userId).getFriends();
        Set<Long> otherUserFriends = users.get(otherId).getFriends();

        Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherUserFriends); // Оставляем только общие ID

        Collection<User> commonFriends = commonFriendIds.stream()
                .map(users::get)
                .collect(Collectors.toList());

        log.info("Общие друзья пользователей с id = {} и id = {}: {}", userId, otherId, commonFriends.size());
        return commonFriends;
    }

    @Override
    public User create(User user) {
        user.setId(getSequence());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с ID = {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        User oldUser = users.get(user.getId());

        if (user.getEmail() != null && !user.getEmail().isBlank() && !user.getEmail().equals(oldUser.getEmail())) {
            if (users.values().stream().anyMatch(anyUser -> anyUser.getEmail().equals(user.getEmail()))) {
                log.warn("Обновление отклонено — email {} уже используется", user.getEmail());
                throw new ConditionsNotMetException("Этот имейл уже использутся");
            }
            oldUser.setEmail(user.getEmail());
        }

        if (user.getLogin() != null && !user.getLogin().isBlank()) {
            oldUser.setLogin(user.getLogin());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            oldUser.setName(user.getName());
        }

        if (user.getBirthday() != null && !user.getBirthday().isAfter(LocalDate.now())) {
            oldUser.setBirthday(user.getBirthday());
        }

        log.info("Пользователь с ID = {} успешно обновлён", user.getId());
        return oldUser;
    }

    @Override
    public void putFriend(Long userId, Long friendId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!users.containsKey(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        User user = users.get(userId);
        User friend = users.get(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь с ID = {} добавил в друзья пользователя с ID = {}", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!users.containsKey(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        User user = users.get(userId);
        User friend = users.get(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь с ID = {} удалил из друзей пользователя с ID = {}", userId, friendId);
    }

    public boolean checkId(Long id) {
        return users.containsKey(id);
    }
}

