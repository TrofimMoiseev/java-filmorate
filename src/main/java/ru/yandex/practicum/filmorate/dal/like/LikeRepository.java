
package ru.yandex.practicum.filmorate.dal.like;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.dal.feed.FeedRepository;
import ru.yandex.practicum.filmorate.dal.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;

import java.util.Collection;

@Slf4j
@Repository
public class LikeRepository extends BaseRepository<Like> {

    private static final String FIND_LIKE_FROM_USER_QUERY = "SELECT * FROM likes WHERE user_id = ? AND film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO likes(user_id, film_id) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
    private static final String FIND_USERS_COMMON_FILMS_QUERY = "SELECT * FROM films f" +
            "    JOIN likes lu on (lu.film_id = f.id)" +
            "    JOIN likes lf on (lf.film_id = f.id)" +
            " WHERE  lu.user_id = ? AND  lf.user_id = ?";

    public LikeRepository(JdbcTemplate jdbc, RowMapper<Like> mapper, FeedRepository feedRepository) {
        super(jdbc, mapper);
    }

    public void putLike(Long userId, Long filmId) {
        log.debug("Запрос лайка от пользователя (Id: {}) на фильм (Id: {})", userId, filmId);
        if (findMany(FIND_LIKE_FROM_USER_QUERY, userId, filmId).isEmpty()) {
            jdbc.update(INSERT_QUERY, userId, filmId);
        }
    }

    public void deleteLike(Long userId, Long filmId) {
        log.debug("Запрос удаления лайка пользователя (Id: {}) с фильма (Id: {})", userId, filmId);
        jdbc.update(DELETE_QUERY, userId, filmId);
    }

    public Collection<Film> findUsersCommonFilms(Long userId, Long friendId) {
        log.debug("Запрос на список фильмов, которые понравились пользователям {} и {}", userId, friendId);
        return jdbc.query(FIND_USERS_COMMON_FILMS_QUERY, new FilmRowMapper(), userId, friendId);
    }
}

