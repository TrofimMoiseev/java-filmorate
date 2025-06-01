package ru.yandex.practicum.filmorate.dal.feed;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.DTO.FeedDTO;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<FeedDTO> {
    public FeedDTO mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        FeedDTO feed = new FeedDTO();
        feed.setEventId(resultSet.getLong("id"));
        feed.setUserId(resultSet.getLong("user_id"));
        feed.setEntityId(resultSet.getLong("entity_id"));
        feed.setEventType(resultSet.getString("event_type"));
        feed.setOperation(resultSet.getString("operation"));
        feed.setTimestamp(resultSet.getLong("timestamp"));
        return feed;
    }
}
