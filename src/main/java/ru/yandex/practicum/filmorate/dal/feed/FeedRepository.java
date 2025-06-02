package ru.yandex.practicum.filmorate.dal.feed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.DTO.FeedDTO;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
public class FeedRepository extends BaseRepository<FeedDTO> {
    private static final String INSERT_QUERY = "INSERT INTO feed (user_id, entity_id, event_id, operation_id, " +
            "timestamp)" + "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_FEEDS_BY_USER_ID_QUERY = "SELECT f.id, f.user_id, f.entity_id, " +
            "e.name AS event_type, o.name AS operation, f.timestamp FROM feed f " +
            "JOIN event_type e ON f.event_id = e.id " +
            "JOIN operation o on f.operation_id = o.id " +
            "WHERE USER_ID = ?";


    public FeedRepository(JdbcTemplate jdbc, RowMapper<FeedDTO> mapper) {
        super(jdbc, mapper);
    }

    public void create(Feed feed) {
        jdbc.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_QUERY);
            preparedStatement.setLong(1, feed.getUserId());
            preparedStatement.setLong(2, feed.getEntityId());
            preparedStatement.setLong(3, feed.getEventId());
            preparedStatement.setLong(4, feed.getOperationId());
            preparedStatement.setLong(5, feed.getTimestamp());
            return preparedStatement;
        });
    }

    public List<FeedDTO> getFeeds(Long id) {
        return findMany(GET_FEEDS_BY_USER_ID_QUERY, id);
    }


}
