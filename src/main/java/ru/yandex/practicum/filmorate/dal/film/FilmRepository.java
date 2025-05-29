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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
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

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper, LikeRepository likeRepository) {
        super(jdbc, mapper);
        this.likeRepository = likeRepository;
    }


    @Override
    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
            Optional<Film> thisFilm = findOne(FIND_BY_ID_QUERY, id);
            thisFilm.ifPresent(this::setGenreAndRatingToFilm);
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
        setGenreAndRatingToFilm(film);
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

        setGenreAndRatingToFilm(film);

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
    public Collection<Film> findPopular(int count) {
        log.debug("Запрос популярных фильмов в хранилище");
        return likeRepository.findPopularFilms(count);
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
}