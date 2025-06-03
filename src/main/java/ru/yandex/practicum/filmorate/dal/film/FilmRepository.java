package ru.yandex.practicum.filmorate.dal.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.dal.like.LikeRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private final LikeRepository likeRepository;

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name, description, release_date, duration, rating_id FROM films WHERE id=?";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String COUNT_OF_RATINGS_QUERY = "SELECT COUNT(*) FROM rating_mpa WHERE id = ?";
    private static final String FIND_GENRES_BY_FILM_QUERY = """
            SELECT g.id, g.name FROM genre g JOIN film_genre fg
            ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id ASC
            """;
    private static final String FIND_MPA_RATINGS_QUERY = "SELECT id, name FROM rating_mpa WHERE id = ?";
    private static final String INSERT_TO_FILM_GENRES_TABLE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name=?, description=?, release_date=?, duration=?, rating_id=? WHERE id=?";
    private static final String CHECK_FILM_ID = "SELECT COUNT(*) FROM films WHERE id = ?";
    private static final String FIND_DIRECTORS_BY_FILM_QUERY = """
            SELECT g.id, g.name FROM director g
            JOIN film_director fg ON g.id = fg.director_id
            WHERE fg.film_id = ?
            """;
    private static final String INSERT_TO_FILM_DIRECTORS_TABLE_QUERY = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
    private static final String FIND_FILMS_BY_DIRECTOR_ID_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id
            FROM films f
            LEFT JOIN film_director fl ON fl.film_id = f.id
            LEFT JOIN likes l ON l.film_id = f.id
            WHERE fl.director_id=?
            GROUP BY f.id
            """;
    private static final String FIND_FILMS_DIRECTORS_BY_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id
            FROM films f
            LEFT JOIN film_director fd ON f.id = fd.film_id
            LEFT JOIN director d ON fd.director_id = d.id
            LEFT JOIN likes l ON f.id = l.film_id
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
    private static final String DELETE_QUERY = "DELETE FROM films WHERE id = ?";

    private static final String FIND_POPULAR = """
    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id
    FROM films f
    LEFT JOIN likes l ON f.id = l.film_id
    LEFT JOIN film_genre fg ON f.id = fg.film_id
    WHERE (? IS NULL OR fg.genre_id = ?)
      AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?)
    GROUP BY f.id
    ORDER BY COUNT(l.user_id) DESC
    LIMIT ?
    """;


    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper, LikeRepository likeRepository) {
        super(jdbc, mapper);
        this.likeRepository = likeRepository;
    }


    @Override
    public List<Film> findAll() {
        List<Film> films = findMany(FIND_ALL_QUERY);

        for (Film film : films) {
            setGenreAndRatingToFilm(film);
            setDirectorsToFilm(film);
        }
        return films;
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
            Optional<Film> thisFilm = findOne(FIND_BY_ID_QUERY, id);
            thisFilm.ifPresent(this::setGenreAndRatingToFilm);
            thisFilm.ifPresent(this::setDirectorsToFilm);
            return thisFilm;
    }

    @Override
    public Film create(Film film) {
        log.info("Добавление фильма {} в репозитории", film);
        long mpaId = film.getMpa().getId();
        Integer count = jdbc.queryForObject(COUNT_OF_RATINGS_QUERY, Integer.class, mpaId);
        if (count == null || count == 0) {
            throw new NotFoundException("Рейтинг с таким ID " + mpaId + " не найден");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_QUERY,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, mpaId);
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        jdbc.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .toList();

            String inSql = genreIds.stream()
                    .map(id -> "?")
                    .collect(Collectors.joining(", "));
            List<Long> existingGenreIds = jdbc.queryForList(
                    "SELECT id FROM genre WHERE id IN (" + inSql + ")",
                    Long.class,
                    genreIds.toArray()
            );

            for (Long genreId : genreIds) {
                if (!existingGenreIds.contains(genreId)) {
                    throw new NotFoundException("Жанр с ID " + genreId + " не найден ");
                }
            }

            for (Long genreId : genreIds) {

                jdbc.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", film.getId(), genreId);
            }
        }

        jdbc.update("DELETE FROM film_director WHERE film_id = ?", film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            List<Long> directorIds = film.getDirectors().stream()
                    .map(Director::getId)
                    .toList();

            String inSql = directorIds.stream()
                    .map(id -> "?")
                    .collect(Collectors.joining(", "));
            List<Long> existingDirectorIds = jdbc.queryForList(
                    "SELECT id FROM director WHERE id IN (" + inSql + ")",
                    Long.class,
                    directorIds.toArray()
            );

            for (Long directorId : directorIds) {
                if (!existingDirectorIds.contains(directorId)) {
                    throw new NotFoundException("Режиссер с ID " + directorId + " не найден ");
                }
            }

            for (Long directorId : directorIds) {

                jdbc.update("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)", film.getId(), directorId);
            }
        }

        setGenreAndRatingToFilm(film);
        setDirectorsToFilm(film);

        log.debug("Фильм {} был добавлен в базу данных", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        log.debug("Обновление фильма {} в репозитории", film);

        Integer count = jdbc.queryForObject(CHECK_FILM_ID, Integer.class, film.getId());
        if (count == 0) {
            throw new NotFoundException("Фильм с id не удалось найти" + film.getId());
        }

        jdbc.update(
                UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbc.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                jdbc.update(INSERT_TO_FILM_GENRES_TABLE_QUERY, film.getId(), genre.getId());
            }
        }

        jdbc.update("DELETE FROM film_director WHERE film_id = ?", film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {

            for (Director director : film.getDirectors()) {
                jdbc.update(INSERT_TO_FILM_DIRECTORS_TABLE_QUERY, film.getId(), director.getId());
            }
        }

        setGenreAndRatingToFilm(film);
        setDirectorsToFilm(film);

        log.debug("Фильм {} был обновлен в базе данных", film);

        return film;
    }

    @Override
    public void putLike(Long userId, Long filmId) {
        log.debug("Добавление лайка в хранилище");
        likeRepository.putLike(userId, filmId);
    }

    @Override
    public void deleteLike(Long userId, Long filmId) {
        log.debug("Удаление лайка в хранилище");
        likeRepository.deleteLike(userId, filmId);
    }

    @Override
    public Collection<Film> findPopular(int count, Integer genreId, Integer year) {
        log.debug("Запрос популярных фильмов с count={}, genreId={}, year={}", count, genreId, year);

        return findMany(FIND_POPULAR,
                genreId, genreId,
                year, year,
                count
        ).stream().peek(film -> {
            setGenreAndRatingToFilm(film);
            setDirectorsToFilm(film);
        }).toList();
    }


    @Override
    public Collection<Film> findFilmsByDirectorId(Long id, String sortBy) {
        log.debug("Запрос фильмов и сортировка фильмов по айди режиссера");
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            throw new ValidationException("Параметр sortBy может быть только 'year' или 'likes'");
        }

        String sql = FIND_FILMS_BY_DIRECTOR_ID_QUERY;
        if (sortBy.equals("year")) {
            sql += "ORDER BY FORMATDATETIME(f.release_date, 'yyyy')";
        } else {
            sql += "ORDER BY COUNT(DISTINCT l.user_id) DESC";
        }

        Collection<Film> films = findMany(sql, id);
        for (Film film : films) {
            setGenreAndRatingToFilm(film);
            setDirectorsToFilm(film);
        }

        return films;
    }

    @Override
    public Collection<Film> findFilmsDirectorsByQuery(String query, String by) {
        log.debug("Запрос фильмов и сортировка фильмов по айди режиссера");

        String sql = FIND_FILMS_DIRECTORS_BY_QUERY;
        ArrayList<String> params = new ArrayList<>();
        if (by.equals("director,title") || by.equals("title,director")) {
            sql += "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%'))\n" +
                    "OR LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%'))";
            params.add(query);
            params.add(query);
        } else if (by.equals("director")) {
            sql += "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%'))";
            params.add(query);
        } else if (by.equals("title")) {
            sql += "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%'))";
            params.add(query);
        } else {
            throw new ValidationException("Параметр by может быть только 'director', 'title', 'title,director', 'director,title'");
        }

        sql += " GROUP BY f.id ORDER BY COUNT(distinct l.user_id) DESC";

        Collection<Film> films = findMany(sql, params.toArray());
        for (Film film : films) {
            setGenreAndRatingToFilm(film);
            setDirectorsToFilm(film);
        }

        return films;
    }

    @Override
    public boolean checkId(Long id) {
        return checkId(CHECK_FILM_ID, id);
    }

    @Override
    public Collection<Film> findCommonFilms(Long userId, Long friendId) {
        Collection<Film> commonFilms = likeRepository.findUsersCommonFilms(userId, friendId);
        commonFilms.forEach(this::setGenreAndRatingToFilm);

        return commonFilms;
    }

    @Override
    public void deleteFilm(Long filmId) {
        log.info("Обработка DELETE-запрос на удаление фильма в хранилище");
        delete(DELETE_QUERY, filmId);
    }

    private void setGenreAndRatingToFilm(Film film) {
        Mpa mpa = jdbc.queryForObject(FIND_MPA_RATINGS_QUERY, (rs, rowNum) -> {
            Mpa mpaObj = new Mpa();
            mpaObj.setId(rs.getLong("id"));
            mpaObj.setName(rs.getString("name"));
            return mpaObj;
        }, film.getMpa().getId());
        film.setMpa(mpa);

        List<Genre> genres = jdbc.query(FIND_GENRES_BY_FILM_QUERY, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void setDirectorsToFilm(Film film) {
        Set<Director> directors = new HashSet<>(jdbc.query(FIND_DIRECTORS_BY_FILM_QUERY, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getLong("id"));
            director.setName(rs.getString("name"));
            return director;
        }, film.getId()));

        film.setDirectors(directors);
    }

    public List<Film> findRecommendationsByUser(Long similarUserId, Long userId) {
        return findMany(RECOMMENDATION_QUERY,similarUserId,userId);
    }
}