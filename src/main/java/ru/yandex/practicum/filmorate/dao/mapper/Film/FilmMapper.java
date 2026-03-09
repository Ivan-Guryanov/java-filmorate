package ru.yandex.practicum.filmorate.dao.mapper.Film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class FilmMapper<U> implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate") != null ?
                        rs.getDate("releaseDate").toLocalDate() : null)
                .duration(rs.getInt("duration"))
                .likes(new HashSet<>())
                .genres(new HashSet<>())
                .mpa(Mpa.builder()
                        .id(rs.getInt("rating_id"))
                        .build())
                .build();
    }

}