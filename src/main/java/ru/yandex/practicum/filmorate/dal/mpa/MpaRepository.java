
package ru.yandex.practicum.filmorate.dal.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaceStorage.MpaStorage;

import java.util.Collection;

@Slf4j
@Repository
public class MpaRepository extends BaseRepository<Mpa> implements MpaStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM rating_mpa";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM rating_mpa WHERE id = ?";

    public MpaRepository(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Mpa> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Mpa findById(Long id) {
        log.info("Ищем рейтинг в репозитории по id {}", id);
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Данного рейтинга нет в списке"));
    }

}
