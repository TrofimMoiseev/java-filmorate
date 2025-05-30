package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director findDirectorById(Long id) {
        log.info("Обработка GET-запроса на получение режиссера по айди.");
        return directorStorage.findDirectorById(id)
                .orElseThrow(() -> {
                    log.warn("Режиссер с id = {} не найден", id);
                    return new NotFoundException("Режиссер с id = " + id + " не найден");
                });
    }

    public Collection<Director> findAll() {
        log.info("Обработка GET-запроса на получение всех режиссеров.");
        return directorStorage.findAll();
    }

    public Director create(Director director) {
        log.info("Обработка POST-запроса на добавление режиссера: {}", director);
        check(director);
        return directorStorage.create(director);
    }

    public Director update(Director newDirector) {
        log.info("Обработка PUT-запрос на обновление режиссера: {}", newDirector);
        if (newDirector.getId() == null) {
            log.warn("Обновление отклонено — ID не указан");
            throw new ConditionsNotMetException("Id не указан");
        }

        Director director = directorStorage.findDirectorById(newDirector.getId())
                .orElseThrow(() -> {
                    log.warn("Режиссер с id = {} не найден", newDirector.getId());
                    return new NotFoundException("Режиссер с id = " + newDirector.getId() + " не найден");
                });

        check(newDirector);
        director.setName(newDirector.getName());

        return directorStorage.update(director);
    }

    public void deleteDirector(Long id) {
        if (!directorStorage.checkId(id)) {
            log.warn("Режиссер с id = {}, не найден", id);
            throw new NotFoundException("Режиссер с id = " + id + " не найден");
        }

        log.info("Режиссер с id {}, удален.", id);
        directorStorage.deleteDirector(id);
    }

    private void check(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            log.warn("Валидация не пройдена — имя режиссера отсутствует");
            throw new ValidationException("Имя указано неверно");
        }
    }
}
