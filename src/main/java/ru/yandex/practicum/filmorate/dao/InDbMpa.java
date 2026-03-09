package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;


@Slf4j
@Component
@RequiredArgsConstructor
public class InDbMpa {
    private final JdbcTemplate jdbc;

    public Mpa inMpaById(Integer mpaId) {
        String sqlMpa = "SELECT * FROM rating WHERE id = ?";
        try {
            return jdbc.queryForObject(sqlMpa, (rs, rowNum) -> {
                Mpa mpa = new Mpa();
                mpa.setId(rs.getInt("id"));
                mpa.setName(rs.getString("rating"));
                return mpa;
            }, mpaId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Mpa с id " + mpaId + " не найден");
        }
    }

    public Collection<Mpa> inMpaAll() {
        String sql = "SELECT * FROM rating";

        try {
            return jdbc.query(sql, (rs, rowNum) -> {
                Mpa mpa = new Mpa();
                mpa.setId(rs.getInt("id"));
                mpa.setName(rs.getString("rating"));
                return mpa;
            });

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при получении списка рейтингов: {}", e.getMessage());
            throw new RuntimeException("Ошибка БД при получении MPA", e);
        }
    }
}