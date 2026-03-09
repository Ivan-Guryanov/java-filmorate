package ru.yandex.practicum.filmorate.dao.mapper.Film;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashSet;
import java.util.LinkedHashSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmDtoMapper<U> {

    public static FilmDto mapToDto(Film film) {
        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .likes(film.getLikes() != null ? film.getLikes() : new HashSet<>())
                .genres(film.getGenres() != null ? film.getGenres() : new LinkedHashSet<>())
                .mpa(film.getMpa())
                .build();

    }
}