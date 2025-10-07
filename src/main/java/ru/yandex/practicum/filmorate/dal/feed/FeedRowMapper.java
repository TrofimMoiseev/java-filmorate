package ru.yandex.practicum.filmorate.dal.feed;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {
    public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEventId(resultSet.getLong("id"));
        feed.setUserId(resultSet.getLong("user_id"));
        feed.setEntityId(resultSet.getLong("entity_id"));
        feed.setEventType(EventType.valueOf(resultSet.getString("event_type")));
        feed.setOperation(Operation.valueOf(resultSet.getString("operation")));
        feed.setTimestamp(resultSet.getLong("timestamp"));
        return feed;
    }
}
