package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping("/{id}")
    public Director findFilmById(
            @PathVariable Long id
    ) {
        log.info("Получен GET-запрос на получение режиссера по айди.");
        return directorService.findDirectorById(id);
    }

    @GetMapping
    public Collection<Director> findAll() {
        log.info("Получен GET-запрос на получение всех режиссеров.");
        return directorService.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Director create(@RequestBody Director director) {
        log.info("Получен POST-запрос на добавление режиссера: {}", director);
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        log.info("Получен PUT-запрос на обновление режиссера: {}", director);
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirector(@PathVariable("id") Long directorId) {
        log.info("Получен Delete-запрос на удаление режиссера");
        directorService.deleteDirector(directorId);
    }
}
