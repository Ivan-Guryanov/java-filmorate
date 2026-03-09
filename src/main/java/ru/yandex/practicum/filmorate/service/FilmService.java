package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.InDbFilmStorage;
import ru.yandex.practicum.filmorate.dao.InDbMpa;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmDto;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmDtoMapper;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final JdbcTemplate jdbc;
    private final FilmMapper mapper;
    private final InDbMpa inDbMpa;
    private final InDbFilmStorage inDbFilmStorage;


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
        log.info("Запрошены {} популярных фильмов", count);
        String sql = "SELECT f.* " +
                "FROM film AS f " +
                "LEFT JOIN likes AS l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";

        try {
            List<Film> films = jdbc.query(sql, mapper, count);

            for (Film film : films) {
                String sqlGenres = "SELECT g.id, g.genre FROM genre g " +
                        "JOIN film_genres fg ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ?";

                List<Genre> genres = jdbc.query(sqlGenres, (rs, rowNum) -> Genre.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("genre"))
                        .build(), film.getId());

                film.setGenres(new LinkedHashSet<>(genres));

                film.setMpa(inDbMpa.inMpaById(film.getMpa().getId()));
            }

            return films.stream()
                    .map(FilmDtoMapper::mapToDto)
                    .toList();


        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при получении списка фильмов: {}", e.getMessage());
            throw new RuntimeException("Ошибка работы с базой данных при получении списка фильмов");
        }
//        } catch (
//                org.springframework.dao.DataAccessException e) {
//            log.error("КРИТИЧЕСКАЯ ОШИБКА: ", e);
//            throw new RuntimeException("Детали ошибки: " + e.getMostSpecificCause().getMessage());
//        }

    }
}