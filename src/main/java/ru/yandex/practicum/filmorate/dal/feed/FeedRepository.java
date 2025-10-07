package ru.yandex.practicum.filmorate.dal.feed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
public class FeedRepository extends BaseRepository<Feed> {
    private static final String INSERT_QUERY = "INSERT INTO feed (user_id, entity_id, event_type, operation, " +
            "timestamp)" + "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_FEEDS_BY_USER_ID_QUERY = "SELECT f.id, f.user_id, f.entity_id, " +
            "f.event_type, f.operation, f.timestamp FROM feed f " +
            "WHERE USER_ID = ?";


    public FeedRepository(JdbcTemplate jdbc, RowMapper<Feed> mapper) {
        super(jdbc, mapper);
    }

    public void create(Feed feed) {
        jdbc.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_QUERY);
            preparedStatement.setLong(1, feed.getUserId());
            preparedStatement.setLong(2, feed.getEntityId());
            preparedStatement.setString(3, String.valueOf(feed.getEventType()));
            preparedStatement.setString(4, String.valueOf(feed.getOperation()));
            preparedStatement.setLong(5, feed.getTimestamp());
            return preparedStatement;
        });
    }

    public List<Feed> getFeeds(Long id) {
        return findMany(GET_FEEDS_BY_USER_ID_QUERY, id);
    }


}
