
package ru.yandex.practicum.filmorate.storage.dbStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.friendship.FriendshipRepository;
import ru.yandex.practicum.filmorate.dal.user.UserRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.UserStorage;

import java.util.Collection;

@Slf4j
@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @Override
    public Collection<User> findAll() {
        log.debug("Запрос всех пользователей в хранилище");
        return userRepository.findAllUsers();
    }

    @Override
    public User findUserById(Long id) {
        log.debug("Запрос пользователя по Id в хранилище");
        return userRepository.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        log.debug("Запрос списка друзей пользователя в хранилище");
        return friendshipRepository.findFriendsByUserId(userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        log.debug("Запрос списка общих друзей пользователей в хранилище");
        return friendshipRepository.findCommonFriends(userId, friendId);
    }

    @Override
    public User create(User user) {
        log.debug("Создание пользователя в хранилище");
        return userRepository.save(user);
    }

    @Override
    public User update(User newUser) {
        log.debug("Обновление пользователя в хранилище");
        User oldUser = userRepository.findUserById(newUser.getId()).orElseThrow(() -> new NotFoundException("Нема такого"));
        System.out.println(oldUser.toString());
        log.debug("Обновление пользователя: email={}, login={}, name={}, birthday={}, id={}",
                newUser.getEmail(), newUser.getLogin(), newUser.getName(), newUser.getBirthday(), newUser.getId());
        return userRepository.update(newUser);
    }


    @Override
    public void putFriend(Long userId, Long friendId) {
        log.debug("Добавление пользователей в друзья в хранилище");
        friendshipRepository.putFriend(userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        log.debug("Запрос удаления из друзей от пользователя в хранилище");
        friendshipRepository.deleteFriend(userId, friendId);
    }

    @Override
    public boolean checkId(Long id) {
        return userRepository.checkId(id);
    }
}
