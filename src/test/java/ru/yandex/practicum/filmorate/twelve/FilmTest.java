package ru.yandex.practicum.filmorate.twelve;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.InDbFilmStorage;
import ru.yandex.practicum.filmorate.dao.InDbUserStorage;
import ru.yandex.practicum.filmorate.dao.mapper.Film.FilmDto;
import ru.yandex.practicum.filmorate.dao.mapper.user.UserDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;


@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmTest {
    private final InDbFilmStorage filmStorage;
    private final InDbUserStorage userStorage;
    private final FilmService filmService;

    @Test
    @DisplayName("Создание фильма: валидные данные + возврат фильма")
    public void shouldCreateFilm() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");
        film.setGenres(Set.of(genre));

        FilmDto savedFilm = filmStorage.create(film);
        long id = savedFilm.getId();

        Optional<FilmDto> filmDtoOptional = Optional.ofNullable(filmStorage.getFilmById(id));

        assertThat(filmDtoOptional)
                .isPresent()
                .hasValueSatisfying(filmDto -> {
                    assertThat(filmDto).hasFieldOrPropertyWithValue("id", id);
                    assertThat(filmDto).hasFieldOrPropertyWithValue("name", "Name");
                    assertThat(filmDto).hasFieldOrPropertyWithValue("description", "Description");
                    assertThat(filmDto).hasFieldOrPropertyWithValue("duration", 120);
                    assertThat(filmDto.getMpa())
                            .isNotNull()
                            .hasFieldOrPropertyWithValue("id", 1)
                            .hasFieldOrPropertyWithValue("name", "G");
                    assertThat(filmDto.getGenres())
                            .isNotNull()
                            .hasSize(1)
                            .anySatisfy(g -> {
                                assertThat(g).hasFieldOrPropertyWithValue("id", 1);
                                assertThat(g).hasFieldOrPropertyWithValue("name", "Комедия");
                            });
                });
    }

    @Test
    @DisplayName("Изменение фильма: валидные данные")
    void shouldUpdateFilm() throws Exception {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");
        film.setGenres(Set.of(genre));

        FilmDto savedFilm = filmStorage.create(film);
        long id = savedFilm.getId();

        Film film2 = new Film();
        film2.setId(id);
        film2.setName("Name2");
        film2.setDescription("Description2");
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setDuration(122);

        Mpa mpa2 = new Mpa();
        mpa2.setId(2);
        film2.setMpa(mpa2);

        Genre genre2 = new Genre();
        genre2.setId(2);
        genre2.setName("Драма");
        film2.setGenres(Set.of(genre2));

        FilmDto updateFilm = filmStorage.update(film2);


        Optional<FilmDto> filmDtoOptional = Optional.ofNullable(filmStorage.getFilmById(id));

        assertThat(filmDtoOptional)
                .isPresent()
                .hasValueSatisfying(filmDto -> {
                    assertThat(filmDto).hasFieldOrPropertyWithValue("id", id);
                    assertThat(filmDto).hasFieldOrPropertyWithValue("name", "Name2");
                    assertThat(filmDto).hasFieldOrPropertyWithValue("description", "Description2");
                    assertThat(filmDto).hasFieldOrPropertyWithValue("duration", 122);
                    assertThat(filmDto.getMpa())
                            .isNotNull()
                            .hasFieldOrPropertyWithValue("id", 2)
                            .hasFieldOrPropertyWithValue("name", "PG");
                    assertThat(filmDto.getGenres())
                            .isNotNull()
                            .hasSize(1)
                            .anySatisfy(g -> {
                                assertThat(g).hasFieldOrPropertyWithValue("id", 2);
                                assertThat(g).hasFieldOrPropertyWithValue("name", "Драма");
                            });
                });
    }

    @Test
    @DisplayName("Получение списка всех фильмов")
    void shouldReturnAllFilms() throws Exception {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");
        film.setGenres(Set.of(genre));

        filmStorage.create(film);

        Film film2 = new Film();
        film2.setName("Name2");
        film2.setDescription("Description2");
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setDuration(122);

        Mpa mpa2 = new Mpa();
        mpa2.setId(2);
        film2.setMpa(mpa2);

        Genre genre2 = new Genre();
        genre2.setId(2);
        genre2.setName("Драма");
        film2.setGenres(Set.of(genre2));

        filmStorage.create(film2);

        Collection<FilmDto> films = filmStorage.findAll();

        assertThat(films)
                .isNotNull()
                .hasSize(2)
                .extracting(FilmDto::getName)
                .containsExactlyInAnyOrder("Name", "Name2");
    }

    @Test
    @DisplayName("Добавление лайка: корректные id фильма и пользователя")
    void shouldAddLikeCorrectly() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");
        film.setGenres(Set.of(genre));

        FilmDto savedFilm = filmStorage.create(film);

        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        UserDto savedUser = userStorage.create(user);

        FilmDto filmAfterLike = filmService.addLike(savedFilm.getId(), savedUser.getId());

        assertThat(filmAfterLike).isNotNull();
        assertThat(filmAfterLike.getId()).isEqualTo(savedFilm.getId());

        assertThat(filmAfterLike.getLikes())
                .isNotNull()
                .contains(savedUser.getId());
    }

    @Test
    @DisplayName("Ошибка при добавлении лайка: несуществующий фильм")
    void shouldThrowExceptionWhenFilmNotFound() {

        assertThrows(RuntimeException.class, () -> {
            filmService.addLike(9999L, 1L);
        });
    }

    @Test
    @DisplayName("Удаление лайка: фильм и пользователь существуют")
    void shouldDeleteLikeCorrectly() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");
        film.setGenres(Set.of(genre));

        FilmDto savedFilm = filmStorage.create(film);

        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        UserDto savedUser = userStorage.create(user);

        filmService.addLike(savedFilm.getId(), savedUser.getId());

        FilmDto filmAfterDelete = filmService.deleteLike(savedFilm.getId(), savedUser.getId());

        assertThat(filmAfterDelete).isNotNull();

        assertThat(filmAfterDelete.getLikes())
                .isNotNull()
                .doesNotContain(savedUser.getId())
                .isEmpty();
    }

    @Test
    @DisplayName("Получение популярных фильмов: сортировка по количеству лайков")
    void shouldReturnPopularFilmsSortedByLikes() {
        FilmDto filmDto1 = filmStorage.create(createFilmObject("Film One", 1));
        FilmDto filmDto2 = filmStorage.create(createFilmObject("Film Two", 1));

        UserDto user1 = userStorage.create(createUserObject("user1@test.ru", "user1"));
        UserDto user2 = userStorage.create(createUserObject("user2@test.ru", "user2"));

        filmService.addLike(filmDto1.getId(), user1.getId());
        filmService.addLike(filmDto1.getId(), user2.getId());

        filmService.addLike(filmDto2.getId(), user1.getId());

        Collection<FilmDto> popular = filmService.getPopular(10);

        assertThat(popular)
                .extracting(FilmDto::getId)
                .containsExactly(filmDto1.getId(), filmDto2.getId());
    }

    private Film createFilmObject(String name, int mpaId) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);
        return film;
    }

    private User createUserObject(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

}