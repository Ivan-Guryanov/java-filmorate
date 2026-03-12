package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
import ru.yandex.practicum.filmorate.model.Mpa;


import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InDbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmMapper mapper;



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

            return getFilmById(id);

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

        String checkSql = "SELECT id FROM film WHERE id = ?";
        if (!jdbc.queryForRowSet(checkSql, newFilm.getId()).next()) {
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
        }

        try {
            String sqlUpdate = "UPDATE film SET name = ?, description = ?, releaseDate = ?, " +
                    "duration = ?, rating_id = ? WHERE id = ?";

            jdbc.update(sqlUpdate,
                    newFilm.getName(),
                    newFilm.getDescription(),
                    newFilm.getReleaseDate(),
                    newFilm.getDuration(),
                    newFilm.getMpa().getId(),
                    newFilm.getId()
            );

            Long id = newFilm.getId();

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

                String sqlGetFullGenres = "SELECT g.id, g.genre FROM genre g " +
                        "JOIN film_genres fg ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ? ORDER BY g.id";

                List<Genre> fullGenres = jdbc.query(sqlGetFullGenres, (rs, rowNum) -> Genre.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("genre"))
                        .build(), id);

                newFilm.setGenres(new LinkedHashSet<>(fullGenres));
            } else {
                newFilm.setGenres(new LinkedHashSet<>());
            }

            log.info("Фильм с id {} успешно обновлен", id);
            return getFilmById(id);

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при работе с БД при обновлении id {}: {}", newFilm.getId(), e.getMessage());
            throw new RuntimeException("Ошибка при обновлении фильма в базе данных", e);
        }
    }

    @Override
    public Collection<FilmDto> findAll() {
        String sql = "SELECT f.*, " +
                "g.id AS genre_id, g.genre AS genre_name, " +
                "r.id AS r_id, r.rating AS r_name " +
                "FROM film f " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "LEFT JOIN genre g ON fg.genre_id = g.id " +
                "LEFT JOIN rating r ON f.rating_id = r.id " +
                "ORDER BY f.id";

        try {
            List<Film> films = jdbc.query(sql, rs -> {
                Map<Long, Film> map = new LinkedHashMap<>();

                while (rs.next()) {
                    Long filmId = rs.getLong("id");
                    Film film = map.get(filmId);

                    if (film == null) {
                        film = Film.builder()
                                .id(filmId)
                                .name(rs.getString("name"))
                                .description(rs.getString("description"))
                                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                                .duration(rs.getInt("duration"))
                                .genres(new LinkedHashSet<>())
                                .mpa(Mpa.builder()
                                        .id(rs.getInt("id"))
                                        .name(rs.getString("rating"))
                                        .build())
                                .build();
                        map.put(filmId, film);
                    }

                    int genreId = rs.getInt("genre_id");
                    if (genreId != 0) {
                        Genre genre = Genre.builder()
                                .id(genreId)
                                .name(rs.getString("genre_name"))
                                .build();
                        film.getGenres().add(genre);
                    }
                }
                return new ArrayList<>(map.values());
            });
            return films.stream()
                    .map(FilmDtoMapper::mapToDto)
                    .toList();

        } catch (
                org.springframework.dao.DataAccessException e) {
            log.error("КРИТИЧЕСКАЯ ОШИБКА: ", e);
            throw new RuntimeException("Детали ошибки: " + e.getMostSpecificCause().getMessage());
        }
    }



    @Override
    public FilmDto getFilmById(Long id) {
        if (id == null) {
            throw new ValidationException("Не указан айди фильма");
        }

        String sql =  "SELECT f.*, " +
                "g.id AS genre_id, g.genre AS genre_name, " +
                "r.id AS r_id, r.rating AS r_name " +
                "FROM film f " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "LEFT JOIN genre g ON fg.genre_id = g.id " +
                "LEFT JOIN rating r ON f.rating_id = r.id " +
                "WHERE f.id = ?";

        try {
            Film resultFilm = jdbc.query(sql, rs -> {
                Film film = null;
                while (rs.next()) {
                    if (film == null) {
                        film = Film.builder()
                                .id(rs.getLong("id"))
                                .name(rs.getString("name"))
                                .description(rs.getString("description"))
                                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                                .duration(rs.getInt("duration"))
                                .genres(new LinkedHashSet<>())
                                .mpa(Mpa.builder()
                                        .id(rs.getInt("r_id")) // Берем алиас рейтинга
                                        .name(rs.getString("r_name"))
                                        .build())
                                .build();
                    }

                    int genreId = rs.getInt("genre_id");
                    if (genreId != 0) {
                        Genre genre = Genre.builder()
                                .id(genreId)
                                .name(rs.getString("genre_name"))
                                .build();
                        film.getGenres().add(genre);
                    }
                }
                return film;
            }, id);

            if (resultFilm == null) {
                log.error("Фильм с id {} не найден в БД", id);
                throw new NotFoundException("Фильм с id " + id + " не найден");
            }

            String likesSql = "SELECT user_id FROM likes WHERE film_id = ?";
            List<Long> userIds = jdbc.query(likesSql, (rs, rowNum) -> rs.getLong("user_id"), id);
            resultFilm.setLikes(new HashSet<>(userIds));

            return FilmDtoMapper.mapToDto(resultFilm);

        } catch (DataAccessException e) {
            log.error("Ошибка при получении фильма id {}: {}", id, e.getMessage());
            throw new RuntimeException("Ошибка работы с базой данных", e);
        }
    }

}