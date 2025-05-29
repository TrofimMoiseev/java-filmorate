
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
    private static final String FIND_POPULAR_FILMS_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, COUNT(fl.user_id) AS likes
            FROM films f LEFT JOIN likes fl ON f.id = fl.film_id GROUP BY f.id ORDER BY likes DESC, f.id ASC LIMIT ?""";

    private static final String FIND_USER_LIKED_FILMS_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id
            FROM films f LEFT JOIN likes fl ON f.id = fl.film_id WHERE fl.user_id = ?""";

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

    public Collection<Film> findPopularFilms(int count) {
        log.debug("Запрос на список {} популярных фильмов", count);
        return jdbc.query(FIND_POPULAR_FILMS_QUERY, new FilmRowMapper(), count);
    }

    public Collection<Film> findUserLikedFilms(Long id) {
        log.debug("Запрос на список {} фильмов, которые понравились пользователю", id);
        return jdbc.query(FIND_USER_LIKED_FILMS_QUERY, new FilmRowMapper(), id);
    }
}

