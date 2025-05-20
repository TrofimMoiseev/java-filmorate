package ru.yandex.practicum.filmorate.dal.like;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Like;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class LikeRowMapper implements RowMapper<Like> {
    @Override
    public Like mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Like like = new Like();
        like.setUserId(resultSet.getLong("user_id"));
        like.setFilmId(resultSet.getLong("film_id"));
        return like;
    }
}