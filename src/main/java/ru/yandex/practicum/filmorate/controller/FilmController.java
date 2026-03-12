package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dao.InDbFilmStorage;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final InDbFilmStorage inDbFilmStorage;
    private final FilmService filmService;

    @GetMapping
    public Collection<FilmDto> findAll() {
        return inDbFilmStorage.findAll();
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable Long id) {

        return inDbFilmStorage.getFilmById(id);
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody Film film) {
        return inDbFilmStorage.create(film);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody Film newFilm) {
        return inDbFilmStorage.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmDto addLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopular(@RequestParam(defaultValue = "10") Integer count) {
        return filmService.getPopular(count);
    }
}
