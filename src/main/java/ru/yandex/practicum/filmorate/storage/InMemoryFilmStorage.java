package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;


    @Override
    public Film create(Film film) {
        log.info("Получен запрос на добавление фильма");

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        film.setId(++currentId);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Получен запрос на изменение фильма");

        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с таким id не существует");
        }

        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        films.put(newFilm.getId(), newFilm);
        log.info("Изменен фильм" + newFilm.getId());
        return newFilm;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) {
        return films.get(id);
    }
}