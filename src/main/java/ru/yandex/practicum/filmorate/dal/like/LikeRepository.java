
package ru.yandex.practicum.filmorate.dal.like;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.dal.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;

import java.util.Collection;

@Slf4j
@Repository
public class LikeRepository extends BaseRepository<Like> {
    private static final String INSERT_QUERY = "INSERT INTO likes(user_id, film_id) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";

    private static final String FIND_USERS_COMMON_FILMS_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id
            FROM ((SELECT l.FILM_ID
            	   FROM likes l
            	   WHERE l.USER_ID = ?) ul1
            	   JOIN (SELECT l.FILM_ID
            	         FROM likes l
            			 WHERE l.USER_ID = ?) ul2 ON ul1.FILM_ID  = ul2.FILM_ID) lf
            LEFT JOIN films f ON f.ID = lf.FILM_ID""";

    public LikeRepository(JdbcTemplate jdbc, RowMapper<Like> mapper) {
        super(jdbc, mapper);
    }

    public void putLike(Long userId, Long filmId) {
        log.debug("Запрос лайка от пользователя (Id: {}) на фильм (Id: {})", userId, filmId);
        jdbc.update(INSERT_QUERY, userId, filmId);
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

