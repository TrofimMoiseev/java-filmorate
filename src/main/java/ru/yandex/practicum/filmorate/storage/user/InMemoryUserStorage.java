package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

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
        log.info("Пользователь предоставлен");
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        log.info("Список предоставлен. Текущее количество пользователей {}.", users.size());
        return users.values();
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
        users.put(user.getId(), user);
        log.info("Пользователь с ID = {} успешно обновлён", user.getId());
        return user;
    }

    public boolean checkId(Long id) {
        return users.containsKey(id);
    }
}

