package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.UserStorage;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;


    public Review create(Review review) {
        validateReview(review);
        review.setUseful(0);
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        Optional<Review> existing = reviewStorage.getById(review.getReviewId());
        if (existing.isEmpty()) {
            throw new ValidationException("Отзыв с id=" + review.getReviewId() + " не найден");
        }
        return reviewStorage.update(review);
    }

    public void delete(Long id) {
        Review review = reviewStorage.getById(id)
                .orElseThrow(() -> new ValidationException("Отзыв с id=" + id + " не найден"));
        reviewStorage.delete(review);
    }

    public Review getById(Long id) {
        return reviewStorage.getById(id)
                .orElseThrow(() -> new ValidationException("Отзыв с id=" + id + " не найден"));
    }

    public List<Review> getAll(Long filmId, Integer count) {
        return reviewStorage.getAll(filmId, count != null ? count : 10);
    }

    public void putLike(Long reviewId, Long userId) {
        validateLikeInput(reviewId, userId);
        reviewStorage.putLike(reviewId, userId);
    }

    public void putDislike(Long reviewId, Long userId) {
        validateLikeInput(reviewId, userId);
        reviewStorage.putDisLike(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        validateLikeInput(reviewId, userId);
        reviewStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        validateLikeInput(reviewId, userId);
        reviewStorage.deleteDisLike(reviewId, userId);
    }

    private void validateReview(Review review) {
        Long userId = review.getUserId();
        Long filmId = review.getFilmId();

        if (!userStorage.checkId(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не существует");
        }
        if (!filmStorage.checkId(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не существует");
        }

    }

    private void validateLikeInput(Long reviewId, Long userId) {
        if (reviewStorage.getById(reviewId).isEmpty()) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
        if (!userStorage.checkId(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}