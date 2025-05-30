package ru.yandex.practicum.filmorate.storage.interfaceStorage;

import ru.yandex.practicum.filmorate.model.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(Review review);

    List<Review> getAll(Long filmId, int count);

    Optional<Review> getById(Long review_id);

    void putLike(Long Id, Long userId);

    void putDisLike(Long Id, Long userId);

    void deleteLike(Long Id, Long userId);

    void deleteDisLike(Long Id, Long userId);
}