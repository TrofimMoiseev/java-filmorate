package ru.yandex.practicum.filmorate.dal.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.interfacestorage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class DirectorRepository extends BaseRepository<Director> implements DirectorStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM director";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM director WHERE id=?";
    private static final String INSERT_QUERY = "INSERT INTO director (name) VALUES (?)";
    private static final String UPDATE_DIRECTOR_QUERY = "UPDATE director SET name=? WHERE id=?";
    private static final String CHECK_DIRECTOR_ID = "SELECT COUNT(*) FROM director WHERE id = ?";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM director WHERE id=?";
    private static final String DELETE_FILM_DIRECTOR_QUERY = "DELETE FROM film_director WHERE director_id=?";

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<Director> findDirectorById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Director create(Director director) {
        log.info("Добавление режиссера {} в репозиторий", director);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_QUERY,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKey().longValue());

        return director;
    }

    @Override
    public Director update(Director director) {
        log.debug("Обновление режиссера {} в репозитории", director);

        jdbc.update(
                UPDATE_DIRECTOR_QUERY,
                director.getName(),
                director.getId()
        );

        log.debug("Режиссер {} был обновлен в базе данных", director);
        return director;
    }

    @Override
    public boolean checkId(Long id) {
        return checkId(CHECK_DIRECTOR_ID, id);
    }

    @Override
    public void deleteDirector(Long id) {
        log.debug("Запрос удаления режиссера (Id: {}).", id);
        delete(DELETE_FILM_DIRECTOR_QUERY, id);
        delete(DELETE_DIRECTOR_QUERY, id);
    }
}
