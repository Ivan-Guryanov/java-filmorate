package ru.yandex.practicum.filmorate.twelve;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.InDbMpa;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaTest {
    private final InDbMpa inDbMpa;

    @Test
    @DisplayName("Получение списка рейтингов")
    void shouldReturnAllMpa() {
        Collection<Mpa> mpa = inDbMpa.inMpaAll();

        assertThat(mpa)
                .isNotNull()
                .hasSize(5)
                .extracting(Mpa::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    @DisplayName("Получение рейтинга по айди")
    void shouldReturnMpaById() {
        Optional<Mpa> mpaOptional = Optional.ofNullable(inDbMpa.inMpaById(1));

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa).hasFieldOrPropertyWithValue("name", "G");
                });
    }
}
