package ru.yandex.practicum.filmorate.storage.dbStorage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.dal.friendship.FriendshipRepository;
import ru.yandex.practicum.filmorate.dal.friendship.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.dal.user.UserRepository;
import ru.yandex.practicum.filmorate.dal.user.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserDbStorage.class,UserRepository.class, FriendshipRepository.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @TestConfiguration
    public static class TestConfig {
        @Bean
        public RowMapper<User> userRowMapper() {
            return new UserRowMapper(); // Реализуйте UserRowMapper
        }

        @Bean
        public RowMapper<Friendship> friendshipRowMapper() {
            return new FriendshipRowMapper(); // Реализуйте FriendshipRowMapper
        }
    }

    @Test
    @DisplayName("Создание и получение пользователя по ID")
    void shouldCreateAndFindUserById() {
        // Создаем нового пользователя через конструктор и сеттеры
        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setLogin("testLogin");
        newUser.setName("Test User");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        // Сохраняем пользователя и находим по ID
        User savedUser = userStorage.create(newUser);
        User foundUser = userStorage.findUserById(savedUser.getId());

        // Проверяем, что данные корректно сохранены
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Обновление пользователя")
    void shouldUpdateUser() {
        // Создаем пользователя
        User user = new User();
        user.setEmail("old@example.com");
        user.setLogin("oldLogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1995, 5, 5));

        // Сохраняем его в хранилище
        User savedUser = userStorage.create(user);

        // Обновляем данные пользователя
        savedUser.setName("New Name");
        savedUser.setEmail("new@example.com");

        // Обновляем пользователя
        User updated = userStorage.update(savedUser);

        // Проверяем, что данные обновлены
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void shouldFindAllUsers() {
        // Создаем и сохраняем пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1991, 1, 1));
        userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        userStorage.create(user2);

        // Получаем всех пользователей и проверяем их количество
        Collection<User> users = userStorage.findAll();

        // Проверяем, что пользователей не меньше двух
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Проверка существования ID")
    void shouldCheckIfUserExistsById() {
        // Создаем и сохраняем пользователя
        User user = new User();
        user.setEmail("exist@example.com");
        user.setLogin("exists");
        user.setName("Exists");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        User savedUser = userStorage.create(user);

        // Проверяем существование пользователя по ID
        assertThat(userStorage.checkId(savedUser.getId())).isTrue();
        assertThat(userStorage.checkId(99999L)).isFalse();
    }
}
