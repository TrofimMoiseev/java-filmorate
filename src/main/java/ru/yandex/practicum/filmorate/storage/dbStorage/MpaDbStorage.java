package ru.yandex.practicum.filmorate.storage.dbStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mpa.MpaRepository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.MpaStorage;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final MpaRepository mpaRepository;

    public Collection<Mpa> findAll() {
        return mpaRepository.findAll();
    }

    public Mpa findById(Long id) {
        return mpaRepository.findById(id);
    }

}
