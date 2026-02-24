package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    @Test
    @DisplayName("Создание фильма: валидные данные")
    void shouldCreateFilm() throws Exception {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    @DisplayName("Ошибка создания: пустое название")
    void sshouldReturnBadRequestWhenNameIsInvalid() throws Exception {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("name: Название не может быть пустым"));
    }

    @Test
    @DisplayName("Ошибка создания: больше 200 символов в описании")
    void sshouldReturnBadRequestWhenDescriptionIsInvalid() throws Exception {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("description: Длина описания должна быть не более 200 символов"));
    }

    @Test
    @DisplayName("Ошибка создания: дата релиза ранее 28.12.1895")
    void sshouldReturnBadRequestWhenReleaseDateIsInvalid() throws Exception {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    @DisplayName("Ошибка создания: отрицательная продолжительность")
    void sshouldReturnBadRequestWhenDurationIsInvalid() throws Exception {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(-1);

        String userJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("duration: Продолжительность фильма должна быть отрицательным числом"));
    }

    @Test
    @DisplayName("Изменение фильма: валидные данные")
    void shouldUpdateFilm() throws Exception {
        Film oldfilm = new Film();
        oldfilm.setName("oldName");
        oldfilm.setDescription("oldDescription");
        oldfilm.setReleaseDate(LocalDate.of(2000, 1, 2));
        oldfilm.setDuration(121);

        mockMvc.perform(post("/films")
                .content(gson.toJson(oldfilm))
                .contentType(MediaType.APPLICATION_JSON));


        Film film = new Film();
        film.setId(1L);
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(put("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    @DisplayName("Ошибка изменения: пустое название")
    void sshouldReturnBadRequestWhenNameIsInvalid1() throws Exception {
        Film oldfilm = new Film();
        oldfilm.setName("oldName");
        oldfilm.setDescription("oldDescription");
        oldfilm.setReleaseDate(LocalDate.of(2000, 1, 2));
        oldfilm.setDuration(121);

        mockMvc.perform(post("/films")
                .content(gson.toJson(oldfilm))
                .contentType(MediaType.APPLICATION_JSON));


        Film film = new Film();
        film.setId(1L);
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(put("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("name: Название не может быть пустым"));
    }

    @Test
    @DisplayName("Ошибка изменения: больше 200 символов в описании")
    void sshouldReturnBadRequestWhenDescriptionIsInvalid1() throws Exception {
        Film oldfilm = new Film();
        oldfilm.setName("oldName");
        oldfilm.setDescription("oldDescription");
        oldfilm.setReleaseDate(LocalDate.of(2000, 1, 2));
        oldfilm.setDuration(121);

        mockMvc.perform(post("/films")
                .content(gson.toJson(oldfilm))
                .contentType(MediaType.APPLICATION_JSON));


        Film film = new Film();
        film.setId(1L);
        film.setName("Name");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(put("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("description: Длина описания должна быть не более 200 символов"));
    }

    @Test
    @DisplayName("Ошибка изменения: дата релиза ранее 28.12.1895")
    void sshouldReturnBadRequestWhenReleaseDateIsInvalid1() throws Exception {
        Film oldfilm = new Film();
        oldfilm.setName("oldName");
        oldfilm.setDescription("oldDescription");
        oldfilm.setReleaseDate(LocalDate.of(2000, 1, 2));
        oldfilm.setDuration(121);

        mockMvc.perform(post("/films")
                .content(gson.toJson(oldfilm))
                .contentType(MediaType.APPLICATION_JSON));


        Film film = new Film();
        film.setId(1L);
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(put("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    @DisplayName("Ошибка изменения: отрицательная продолжительность")
    void sshouldReturnBadRequestWhenDurationIsInvalid1() throws Exception {
        Film oldfilm = new Film();
        oldfilm.setName("oldName");
        oldfilm.setDescription("oldDescription");
        oldfilm.setReleaseDate(LocalDate.of(2000, 1, 2));
        oldfilm.setDuration(121);

        mockMvc.perform(post("/films")
                .content(gson.toJson(oldfilm))
                .contentType(MediaType.APPLICATION_JSON));


        Film film = new Film();
        film.setId(1L);
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(-1);

        String userJson = gson.toJson(film);

        mockMvc.perform(put("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("duration: Продолжительность фильма должна быть отрицательным числом"));
    }

    @Test
    @DisplayName("Ошибка изменения: пустой айди")
    void sshouldReturnBadRequestWhenIdIsInvalid() throws Exception {
        Film oldfilm = new Film();
        oldfilm.setName("oldName");
        oldfilm.setDescription("oldDescription");
        oldfilm.setReleaseDate(LocalDate.of(2000, 1, 2));
        oldfilm.setDuration(121);

        mockMvc.perform(post("/films")
                .content(gson.toJson(oldfilm))
                .contentType(MediaType.APPLICATION_JSON));


        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 01, 01));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(put("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Id должен быть указан"));
    }

    @Test
    @DisplayName("Ошибка изменения: несуществующий айди")
    void sshouldReturnBadRequestWhenIdIsInvalid1() throws Exception {
        Film oldfilm = new Film();
        oldfilm.setName("oldName");
        oldfilm.setDescription("oldDescription");
        oldfilm.setReleaseDate(LocalDate.of(2000, 1, 2));
        oldfilm.setDuration(121);

        mockMvc.perform(post("/films")
                .content(gson.toJson(oldfilm))
                .contentType(MediaType.APPLICATION_JSON));


        Film film = new Film();
        film.setId(2L);
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 01, 01));
        film.setDuration(120);

        String userJson = gson.toJson(film);

        mockMvc.perform(put("/films")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с таким id не существует"));
    }

    @Test
    @DisplayName("Получение списка всех фильмов")
    void shouldReturnAllFilms() throws Exception {
        // 1. Создаем два разных фильма
        Film film1 = new Film();
        film1.setName("Name1");
        film1.setDescription("Description1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Name2");
        film2.setDescription("Description2");
        film2.setReleaseDate(LocalDate.of(2000, 1, 2));
        film2.setDuration(121);

        // 2. Сохраняем их через POST
        mockMvc.perform(post("/films")
                .content(gson.toJson(film1))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/films")
                .content(gson.toJson(film2))
                .contentType(MediaType.APPLICATION_JSON));

        // 3. Выполняем GET запрос
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                // Проверяем, что в массиве 2 элемента
                .andExpect(jsonPath("$.length()").value(2))
                // Проверяем, что названия присутствуют в ответе
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

}




