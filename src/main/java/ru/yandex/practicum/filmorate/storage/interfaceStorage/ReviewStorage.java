package ru.yandex.practicum.filmorate.storage.interfaceStorage;

import ru.yandex.practicum.filmorate.model.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(Review review);

    List<Review> getAll(Long filmId, int count);

    Optional<Review> getById(Long reviewId);

    Optional<Boolean> getReviewRating(Long reviewId, Long userId);

    void putLike(Long reviewId, Long userId);

    void putDisLike(Long reviewId, Long userId);

    void deleteLike(Long reviewId, Long userId);

    void deleteDisLike(Long reviewId, Long userId);
}