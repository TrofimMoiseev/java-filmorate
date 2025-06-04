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
            return reviewStorage.create(review);
        }

        public Review update(Review review) {
            validateReview(review);
            return reviewStorage.update(review);
        }

        public void delete(Long id) {
            reviewStorage.delete(id);
        }

        public Review findById(Long id) {
            return reviewStorage.findById(id)
                    .orElseThrow(() -> new NotFoundException("Отзыв с id=" + id + " не найден"));
        }

        public List<Review> findAll(Long filmId, Integer count) {
            return reviewStorage.findAll(filmId, count != null ? count : 10);
        }

        public void putLike(Long reviewId, Long userId) {
            validateLikeInput(reviewId, userId);

            Optional<Boolean> currentRating = reviewStorage.getReviewRating(reviewId, userId);
            if (currentRating.isPresent() && currentRating.get()) {
                throw new ValidationException("Пользователь уже поставил лайк");
            }

            reviewStorage.putLike(reviewId, userId);
        }

        public void putDislike(Long reviewId, Long userId) {
            validateLikeInput(reviewId, userId);

            Optional<Boolean> currentRating = reviewStorage.getReviewRating(reviewId, userId);

            if (currentRating.isPresent()) {
                if (!currentRating.get()) {
                    throw new ValidationException("Пользователь уже поставил дизлайк");
                } else {
                    reviewStorage.deleteLike(reviewId, userId);
                }
            }
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
            if (review.getUserId() == null) {
                throw new ValidationException("Пользователь не указан (userId=null)");
            }
            if (review.getFilmId() == null) {
                throw new ValidationException("Фильм не указан (filmId=null)");
            }
            if (review.getIsPositive() == null) {
                throw new ValidationException("Тип отзыва не указан (isPositive=null)");
            }
            if (review.getContent() == null || review.getContent().isBlank()) {
                throw new ValidationException("Содержание отзыва не указано (content=null)");
            }

            if (!userStorage.checkId(review.getUserId())) {
                throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не существует");
            }

            if (!filmStorage.checkId(review.getFilmId())) {
                throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не существует");
            }
        }

        private void validateLikeInput(Long reviewId, Long userId) {
            if (reviewStorage.findById(reviewId).isEmpty()) {
                throw new ValidationException("Отзыв с id=" + reviewId + " не найден");
            }
            if (!userStorage.checkId(userId)) {
                throw new ValidationException("Пользователь с id=" + userId + " не найден");
            }
        }
    }