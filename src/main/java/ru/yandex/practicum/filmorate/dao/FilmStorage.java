package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    FilmDto create(Film film);

    FilmDto update(Film newFilm);

    Collection<FilmDto> findAll();

    FilmDto getFilmById(Long id);

}
