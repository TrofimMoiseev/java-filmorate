package ru.yandex.practicum.filmorate.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Review create(@RequestBody Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Удаление отзыва с id={}", id);
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable int id) {
        log.info("Получение отзыва по id={}", id);
        return reviewService.getById(id);
    }

    @GetMapping
    public Collection<Review> findAll(@RequestParam(required = false) Long filmId,
                                      @RequestParam(required = false, defaultValue = "10") Integer count) {
        log.info("Получение отзывов. filmId={}, count={}", filmId, count);
        return reviewService.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Лайк отзыва id={} от пользователя id={}", id, userId);
        reviewService.putLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Дизлайк отзыва id={} от пользователя id={}", id, userId);
        reviewService.putDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка отзыва id={} от пользователя id={}", id, userId);
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление дизлайка отзыва id={} от пользователя id={}", id, userId);
        reviewService.deleteDislike(id, userId);
    }
}
