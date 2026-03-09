package ru.yandex.practicum.filmorate.twelve;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.InDbGenre;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreTest {

    private final InDbGenre inDbGenre;

    @Test
    @DisplayName("Получение списка жанров")
    void shouldReturnAllMpa() {
        Collection<Genre> genre = inDbGenre.inGenryAll();

        assertThat(genre)
                .isNotNull()
                .hasSize(6)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }


    @Test
    @DisplayName("Получение жанра по айди")
    void shouldReturnGenreById() {
        Optional<Genre> genreOptional = Optional.ofNullable(inDbGenre.inGenryById(1));

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия");
                });
    }
}
