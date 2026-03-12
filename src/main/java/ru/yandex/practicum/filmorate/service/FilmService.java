package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.InDbFilmStorage;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmDto;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmDtoMapper;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final JdbcTemplate jdbc;
    private final InDbFilmStorage inDbFilmStorage;
    private final FilmMapper mapper;


    public FilmDto addLike(Long id, Long userId) {
        log.info("Получен запрос от пользователя {} на простановку лайка фильму {}", userId, id);

        String sql = "INSERT INTO likes (film_id, user_id, created_at) VALUES (?, ?, ?)";
        try {
            jdbc.update(sql, id, userId, LocalDate.now());
            log.info("Пользователь {} поставил лайк фильму {}", userId, id);
            return inDbFilmStorage.getFilmById(id);

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при работе с БД при добавление лайка к фильму id {} пользователем {}", id, userId);
            throw new RuntimeException("Ошибка при обновлении фильма в базе данных");
        }
    }

    public FilmDto deleteLike(Long id, Long userId) {
        log.info("Получен запрос от пользователя {} на удаление лайка у фильма {}", userId, id);

        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        try {
            jdbc.update(sql, id, userId);
            log.info("Пользователь {} удалил лайк фильму {}", userId, id);
            return inDbFilmStorage.getFilmById(id);

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при работе с БД при добавление лайка к фильму id {} пользователем {}", id, userId);
            throw new RuntimeException("Ошибка при удаление лайка");
        }

    }

    public Collection<FilmDto> getPopular(Integer count) {
        if (count == null) {
            count = 10;
        }
        log.info("Запрошены {} популярных фильмов", count);
        String sql = "SELECT f.*, r.rating AS mpa_name, COUNT(l.user_id) AS rate " +
                "FROM film f " +
                "LEFT JOIN rating r ON f.rating_id = r.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY rate DESC " +
                "LIMIT ?";

        List<Film> films = jdbc.query(sql, mapper, count);
        return films.stream()
                .map(FilmDtoMapper::mapToDto)
                .collect(Collectors.toList());
    }
}