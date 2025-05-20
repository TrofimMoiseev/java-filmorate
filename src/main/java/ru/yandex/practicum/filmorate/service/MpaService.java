package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;

    public Collection<Mpa> getAllRatings() {
        return mpaStorage.findAll();
    }

    public Mpa findByMpaId(Long id) {
        log.info("Ищем рейтинг в сервисе по id {}", id);
        return mpaStorage.findById(id);
    }
}
