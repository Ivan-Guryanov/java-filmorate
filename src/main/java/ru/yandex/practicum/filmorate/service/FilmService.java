package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addLike(Long id, Long userId) {
        log.info("Получен запрос от пользователя {} на простановку лайка фильму {}", userId, id);

        Film film = filmStorage.getFilmById(id);

        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        if (userStorage.getUsetById(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        film.getLikes().add(userId);

        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
        return film;
    }

    public Film deleteLike(Long id, Long userId) {
        log.info("Получен запрос от пользователя {} на удаление лайка у фильма {}", userId, id);
        Film film = filmStorage.getFilmById(id);

        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        if (userStorage.getUsetById(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        film.getLikes().remove(userId);

        log.info("Пользователь {} удалил лайк у фильма {}", userId, id);
        return film;
    }

    public Collection<Film> getPopular(Integer count) {
        log.info("Запрошены {} популярных фильмов", count);
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}