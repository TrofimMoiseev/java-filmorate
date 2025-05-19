package ru.yandex.practicum.filmorate.dal.friendship;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.dal.user.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

@Slf4j
@Repository
public class FriendshipRepository extends BaseRepository<Friendship> {
    private static final String FIND_FRIENDS_BY_USER_QUERY = "SELECT u.* FROM users u JOIN friendship f ON u.id = f.friend_id WHERE f.user_id = ?";
    private static final String INSERT_QUERY_FOR_ADD_FRIEND = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM friendship WHERE (user_id = ? AND friend_id = ?)";
    private static final String FIND_COMMON_FRIENDS_QUERY = """
            SELECT u.* FROM friendship f1
            JOIN friendship f2 ON f1.friend_id = f2.friend_id
            JOIN users u ON u.id = f1.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            """;


    public FriendshipRepository(JdbcTemplate jdbc, RowMapper<Friendship> mapper) {
        super(jdbc, mapper);
    }

    public List<User> findFriendsByUserId(Long userId) {
        log.debug("Запрос всех пользователей из базы данных");
        return jdbc.query(FIND_FRIENDS_BY_USER_QUERY, new UserRowMapper(), userId);
    }

    public void putFriend(Long userId, Long friendId) {
        log.debug("Добавляем в друзья {} и {}", userId, friendId);
        update(INSERT_QUERY_FOR_ADD_FRIEND, userId, friendId);
        }

    public void deleteFriend(Long userId, Long friendId) {
        log.debug("Запрос удаления пользователя (Id: {}) из списка друзей пользователя (Id: {})", friendId, userId);
        delete(DELETE_QUERY, userId, friendId);
    }

    public Collection<User> findCommonFriends(Long userId, Long friendId) {
        log.debug("Запрос вывода общих друзей пользователя (Id: {}) и пользователя (Id: {})", userId, friendId);
        return jdbc.query(FIND_COMMON_FRIENDS_QUERY, new UserRowMapper(), userId, friendId);
    }
}