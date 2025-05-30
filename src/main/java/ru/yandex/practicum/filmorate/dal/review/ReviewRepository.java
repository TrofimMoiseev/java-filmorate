package ru.yandex.practicum.filmorate.dal.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.ReviewStorage;
import ru.yandex.practicum.filmorate.model.Review;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ReviewRepository extends BaseRepository<Review> implements ReviewStorage {

    private static final String CREATE_REVIEW = """
        INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
        VALUES (?, ?, ?, ?, ?)
    """;

    private static final String UPDATE_REVIEW = """
        UPDATE reviews SET content = ?, is_positive = ? WHERE reviewId = ?
    """;

    private static final String DELETE_REVIEW = "DELETE FROM reviews WHERE reviewId = ?";

    private static final String FIND_BY_ID = "SELECT * FROM reviews WHERE reviewId = ?";

    private static final String FIND_ALL = """
        SELECT * FROM reviews
        WHERE (? IS NULL OR film_id = ?)
        ORDER BY useful DESC
        LIMIT ?
    """;

    private static final String INSERT_REVIEW_RATING = """
        INSERT INTO review_likes (review_id, user_id, is_like)
        VALUES (?, ?, ?)
    """;


    private static final String DELETE_LIKE = """
        DELETE FROM review_likes
        WHERE review_id = ? AND user_id = ? AND is_like = TRUE
    """;

    private static final String DELETE_DISLIKE = """
        DELETE FROM review_likes
        WHERE review_id = ? AND user_id = ? AND is_like = FALSE
    """;

    private static final String CHECK_ALREADY_RATED = """
        SELECT COUNT(*) FROM review_likes
        WHERE review_id = ? AND user_id = ? AND is_like = ?
    """;

    private static final String UPDATE_USEFUL_PLUS = """
        UPDATE reviews SET useful = useful + 1 WHERE reviewId = ?
    """;

    private static final String UPDATE_USEFUL_MINUS = """
        UPDATE reviews SET useful = useful - 1 WHERE reviewId = ?
    """;

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review create(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_REVIEW, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return getById(id).orElseThrow();
    }


    @Override
    public Review update(Review review) {
        jdbc.update(UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        return getById(review.getReviewId()).orElseThrow();
    }

    @Override
    public void delete(Review review) {
        jdbc.update(DELETE_REVIEW, review.getReviewId());
    }

    @Override
    public List<Review> getAll(Long filmId, int count) {
        return jdbc.query(FIND_ALL, mapper, filmId, filmId, count);
    }

    @Override
    public Optional<Review> getById(Long id) {
        List<Review> result = jdbc.query(FIND_BY_ID, mapper, id);
        return result.stream().findFirst();
    }

    @Override
    public void putLike(Long reviewId, Long userId) {
        jdbc.update(DELETE_DISLIKE, reviewId, userId);

        if (!hasAlreadyRated(reviewId, userId, true)) {
            jdbc.update(INSERT_REVIEW_RATING, reviewId, userId, true);
            jdbc.update(UPDATE_USEFUL_PLUS, reviewId);
        }
    }

    @Override
    public void putDisLike(Long reviewId, Long userId) {
        jdbc.update(DELETE_LIKE, reviewId, userId);

        if (!hasAlreadyRated(reviewId, userId, false)) {
            jdbc.update(INSERT_REVIEW_RATING, reviewId, userId, false);
            jdbc.update(UPDATE_USEFUL_MINUS, reviewId);
        }
    }

    @Override
    public Optional<Boolean> getReviewRating(Long reviewId, Long userId) {
        String sql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        List<Boolean> result = jdbc.query(sql, (rs, rowNum) -> rs.getBoolean("is_like"), reviewId, userId);
        return result.stream().findFirst();
    }


    @Override
    public void deleteLike(Long reviewId, Long userId) {
        jdbc.update(DELETE_LIKE, reviewId, userId);
        jdbc.update(UPDATE_USEFUL_MINUS, reviewId);
    }

    @Override
    public void deleteDisLike(Long reviewId, Long userId) {
        jdbc.update(DELETE_DISLIKE, reviewId, userId);
        jdbc.update(UPDATE_USEFUL_PLUS, reviewId);
    }

    private boolean hasAlreadyRated(Long reviewId, Long userId, boolean isLike) {
        Integer count = jdbc.queryForObject(CHECK_ALREADY_RATED, Integer.class, reviewId, userId, isLike);
        return count != null && count > 0;
    }
}