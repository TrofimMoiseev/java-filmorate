package ru.yandex.practicum.filmorate.dal.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.DTO.FeedDTO;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.dal.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.feed.FeedRepository;
import ru.yandex.practicum.filmorate.dal.friendship.FriendshipRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository
public class UserRepository extends BaseRepository<User> implements UserStorage {

    private final FriendshipRepository friendshipRepository;
    private final FeedRepository feedRepository;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String CHECK_USER_ID = "SELECT COUNT(*) FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(login, name, email, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE id = ?";

    private static final String FIND_COMMON_LIKES = """
        SELECT u.id
        FROM likes l1
        JOIN likes l2 ON l1.film_id = l2.film_id
        JOIN users u ON l2.user_id = u.id
        WHERE l1.user_id = ?
        AND u.id != ?
        GROUP BY u.id
        ORDER BY COUNT(*) DESC
        LIMIT 1;
    """;

    private static final String RECOMMENDATION_QUERY = """
    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id
    FROM films f
    JOIN likes l_sim ON f.id = l_sim.film_id
    WHERE l_sim.user_id = ?
    AND f.id NOT IN (
        SELECT film_id FROM likes WHERE user_id = ?
    )
    """;
    
    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper, FriendshipRepository friendshipRepository, FeedRepository feedRepository) {
        super(jdbc, mapper);
        this.friendshipRepository = friendshipRepository;
        this.feedRepository = feedRepository;
    }

    @Override
    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> findUserById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
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
        log.debug("Запрос на добавления пользователя ({}) в базу данных", user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.debug("Пользователь ({}) добавлен в базу данных", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        log.info("Запрос на обновление пользователя в базе данных: email={}, login={}, name={}, birthday={}, id={}",
                user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()), user.getId());

        update(UPDATE_QUERY,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        log.debug("Пользователь ({}) обновлен в базе данных", user.getId());
        return user;
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
    public void deleteUser(Long userId) {
        log.debug("Запрос удаления пользователя в хранилище");
        delete(DELETE_QUERY, userId);
    }

    @Override
    public Collection<Film> getRecommendations(Long userId) {
        log.debug("Получение рекомендаций для пользователя с id={}", userId);

        try {
            Long similarUserId = jdbc.queryForObject(FIND_COMMON_LIKES, Long.class, userId, userId);

            if (similarUserId == null) {
                log.warn("Похожий пользователь не найден для id={}", userId);
                return Collections.emptyList();
            }

            Collection<Film> recommendations = jdbc.query(RECOMMENDATION_QUERY, new FilmRowMapper(), similarUserId, userId);

            if (recommendations.isEmpty()) {
                log.debug("Нет фильмов для рекомендации для пользователя с id={}", userId);
            } else {
                log.debug("Найдено {} фильмов для рекомендации пользователю с id={}", recommendations.size(), userId);
            }

            return recommendations;
        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций для пользователя с id={}", userId, e);
            return Collections.emptyList();
        }
    }


    @Override
    public boolean checkId(Long id) {
        return checkId(CHECK_USER_ID, id);
    }

    @Override
    public List<FeedDTO> getFeeds(Long id) {
        return feedRepository.getFeeds(id);
    }

    ;
}
