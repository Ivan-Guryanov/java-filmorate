package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class InDbGenre {
    private final JdbcTemplate jdbc;

    public Genre inGenryById(Integer genreId) {
        String sqlGenry = "SELECT * FROM genre WHERE id = ?";
        try {
            return jdbc.queryForObject(sqlGenry, (rs, rowNum) -> {
                Genre genre = new Genre();
                genre.setId(rs.getInt("id"));
                genre.setName(rs.getString("genre"));
                return genre;
            }, genreId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с id " + genreId + " не найден");
        }
    }

    public Collection<Genre> inGenryAll() {
        String sql = "SELECT * FROM genre";

        try {
            return jdbc.query(sql, (rs, rowNum) -> {
                Genre genre = new Genre();
                genre.setId(rs.getInt("id"));
                genre.setName(rs.getString("genre"));
                return genre;
            });

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при получении списка жанров: {}", e.getMessage());
            throw new RuntimeException("Ошибка БД при получении жанра", e);
        }
    }

}

