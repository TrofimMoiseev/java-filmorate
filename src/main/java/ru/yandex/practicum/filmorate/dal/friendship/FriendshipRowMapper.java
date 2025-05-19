package ru.yandex.practicum.filmorate.dal.friendship;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {
    @Override
    public Friendship mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Friendship friendship = new Friendship();
        friendship.setUserId(resultSet.getLong("id"));
        friendship.setFriendId(resultSet.getLong("friend_id"));
        friendship.setConfirmStatus(resultSet.getBoolean("confirm_status"));
        return friendship;
    }
}