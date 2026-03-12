package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dao.InDbGenre;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final InDbGenre inDbGenre;

    @GetMapping
    public Collection<Genre> findAll() {
        return inDbGenre.inGenryAll();
    }

    @GetMapping("/{id}")
    public Genre getMpaById(@PathVariable Integer id) {
        return inDbGenre.inGenryById(id);
    }
}