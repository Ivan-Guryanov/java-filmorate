package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmDto;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmDtoMapper;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;


import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InDbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmMapper<Film> mapper;
    private final InDbMpa inDbMpa;


    @Override
    public FilmDto create(Film film) {
        log.info("Получен запрос на добавление фильма");

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        String sql = "INSERT INTO film (name, description, releaseDate, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setObject(3, film.getReleaseDate());
                ps.setInt(4, film.getDuration());
                ps.setInt(5, film.getMpa().getId());
                return ps;
            }, keyHolder);

            Long id = keyHolder.getKeyAs(Long.class);
            film.setId(id);

            log.info("Фильм сохранен в БД с id: {}", id);

            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                String sqlGenres = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

                List<Genre> uniqueGenres = film.getGenres().stream()
                        .distinct()
                        .toList();

                List<Object[]> batchArgs = uniqueGenres.stream()
                        .map(genre -> new Object[]{id, genre.getId()})
                        .toList();

                jdbc.batchUpdate(sqlGenres, batchArgs);

                String sqlGetFullGenres = "SELECT g.id, g.genre FROM genre g " +
                        "JOIN film_genres fg ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ? ORDER BY g.id";

                List<Genre> fullGenres = jdbc.query(sqlGetFullGenres, (rs, rowNum) -> Genre.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("genre"))
                        .build(), id);

                film.setGenres(new LinkedHashSet<>(fullGenres));
            } else {
                film.setGenres(new LinkedHashSet<>());
            }

            film.setMpa(inDbMpa.inMpaById(film.getMpa().getId()));

            return FilmDtoMapper.mapToDto(film);

//        } catch (org.springframework.dao.DataAccessException e) {
//            log.error("Ошибка при работе с БД: {}", e.getMessage());
//            throw new RuntimeException("Ошибка сохранения пользователя");
//        }
        } catch (
                org.springframework.dao.DataAccessException e) {
            log.error("КРИТИЧЕСКАЯ ОШИБКА: ", e);
            throw new RuntimeException("Детали ошибки: " + e.getMostSpecificCause().getMessage());
        }

    }

    @Override
    public FilmDto update(Film newFilm) {
        log.info("Получен запрос на изменение фильма с id: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        // Проверка существования
        String checkSql = "SELECT id FROM film WHERE id = ?";
        if (!jdbc.queryForRowSet(checkSql, newFilm.getId()).next()) {
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
        }

        try {
            String sqlUpdate = "UPDATE film SET name = ?, description = ?, releaseDate = ?, " +
                    "duration = ?, rating_id = ? WHERE id = ?";

            // Используем простой update, так как ID нам уже известен
            jdbc.update(sqlUpdate,
                    newFilm.getName(),
                    newFilm.getDescription(),
                    newFilm.getReleaseDate(),
                    newFilm.getDuration(),
                    newFilm.getMpa().getId(),
                    newFilm.getId()
            );

            Long id = newFilm.getId();

            // ОБЯЗАТЕЛЬНО удаляем старые жанры перед любой проверкой
            jdbc.update("DELETE FROM film_genres WHERE film_id = ?", id);

            if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
                String sqlGenres = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

                List<Genre> uniqueGenres = newFilm.getGenres().stream()
                        .distinct()
                        .toList();

                List<Object[]> batchArgs = uniqueGenres.stream()
                        .map(genre -> new Object[]{id, genre.getId()})
                        .toList();

                jdbc.batchUpdate(sqlGenres, batchArgs);

                // Подгружаем полные данные жанров (с именами) из БД
                String sqlGetFullGenres = "SELECT g.id, g.genre FROM genre g " +
                        "JOIN film_genres fg ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ? ORDER BY g.id";

                List<Genre> fullGenres = jdbc.query(sqlGetFullGenres, (rs, rowNum) -> Genre.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("genre")) // Здесь должно быть имя колонки из вашей БД
                        .build(), id);

                newFilm.setGenres(new LinkedHashSet<>(fullGenres));
            } else {
                newFilm.setGenres(new LinkedHashSet<>());
            }

            // Подгружаем полное MPA с именем
            newFilm.setMpa(inDbMpa.inMpaById(newFilm.getMpa().getId()));

            log.info("Фильм с id {} успешно обновлен", id);
            return FilmDtoMapper.mapToDto(newFilm);

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при работе с БД при обновлении id {}: {}", newFilm.getId(), e.getMessage());
            throw new RuntimeException("Ошибка при обновлении фильма в базе данных", e);
        }
    }

    @Override
    public Collection<FilmDto> findAll() {
        String query = "SELECT * FROM film";

        try {
            List<Film> films = jdbc.query(query, mapper);

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

    }


    @Override
    public FilmDto getFilmById(Long id) {
        if (id == null) {
            throw new ValidationException("Не указан айди фильма");
        }

        String sql = "SELECT * FROM film WHERE id = ?";

        try {
            Film film = jdbc.queryForObject(sql, mapper, id);

            if (film == null) {
                throw new NotFoundException("Фильм с id " + id + " не найден");
            }

            if (film.getMpa() != null && film.getMpa().getId() != 0) {
                film.setMpa(inDbMpa.inMpaById(film.getMpa().getId()));
            }

            String sqlGetGenres = "SELECT g.id, g.genre FROM genre g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.id";

            List<Genre> genres = jdbc.query(sqlGetGenres, (rs, rowNum) -> Genre.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("genre"))
                    .build(), id);

            film.setGenres(new LinkedHashSet<>(genres));


            String likesSql = "SELECT user_id FROM likes WHERE film_id = ?";
            List<Long> userIds = jdbc.query(likesSql, (rs, rowNum) -> rs.getLong("user_id"), id);
            film.setLikes(new HashSet<>(userIds));


            return FilmDtoMapper.mapToDto(film);

        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.error("Фильм с id {} не найден в БД", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при получении фильма id {}: {}", id, e.getMessage());
            throw new RuntimeException("Ошибка работы с базой данных", e);
        }
    }

}

//} catch (
//org.springframework.dao.DataAccessException e) {
//        log.error("КРИТИЧЕСКАЯ ОШИБКА: ", e);
//            throw new RuntimeException("Детали ошибки: " + e.getMostSpecificCause().getMessage());
//        }