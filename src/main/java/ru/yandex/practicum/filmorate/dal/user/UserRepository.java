package ru.yandex.practicum.filmorate.dal.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepository extends BaseRepository<User> {

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String CHECK_USER_ID = "SELECT COUNT(*) FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(login, name, email, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    public List<User> findAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> findUserById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public User save(User user) {
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

    public User update(User user) {
        log.info("Запрос на бновление пользователя в базе данных: email={}, login={}, name={}, birthday={}, id={}",
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

    public boolean checkId(Long id) {
        return checkId(CHECK_USER_ID, id);
    }

}
