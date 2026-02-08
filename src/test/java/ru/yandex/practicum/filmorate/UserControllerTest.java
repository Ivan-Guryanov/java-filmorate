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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerTest {

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
    @DisplayName("Создание пользователя: валидные данные")
    void shouldCreateUser() throws Exception {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        String userJson = gson.toJson(user);

        mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@yandex.ru"));
    }

    @Test
    @DisplayName("Ошибка создания: некорректный email")
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        User user = new User();
        user.setEmail("bad_email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        mockMvc.perform(post("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка создания: пустой емайл")
    void shouldReturnBadRequestWhenEmailIsInvalid1() throws Exception {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        mockMvc.perform(post("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка создания: пустой логин")
    void shouldReturnBadRequestWhenLoginIsInvalid() throws Exception {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        mockMvc.perform(post("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка создания: логин с пробелами")
    void shouldReturnBadRequestWhenLoginIsInvalid1() throws Exception {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("log in");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        mockMvc.perform(post("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание пользователя: пустое имя должно заменяться на логин")
    void shouldSetLoginAsNameIfNameIsEmpty() throws Exception {

        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("");

        mockMvc.perform(post("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("login"))
                .andExpect(jsonPath("$.login").value("login"));
    }

    @Test
    @DisplayName("Ошибка создания: дата рождения в будущем")
    void shouldReturnBadRequestWhenBirthdayIsInFuture() throws Exception {

        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));
        user.setName("Name");

        mockMvc.perform(post("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Изменение пользователя: валидные данные")
    void shouldUpdateCUser() throws Exception {

        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                .content(gson.toJson(initialUser))
                .contentType(MediaType.APPLICATION_JSON));

        User user = new User();
        user.setId(1L);
        user.setEmail("test@yandex.ru");
        user.setLogin("test_login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        String userJson = gson.toJson(user);

        mockMvc.perform(put("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@yandex.ru"));
    }

    @Test
    @DisplayName("Ошибка изменения: некорректный email")
    void shouldUpdateBadRequestWhenEmailIsInvalid() throws Exception {

        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                .content(gson.toJson(initialUser))
                .contentType(MediaType.APPLICATION_JSON));

        User user = new User();
        user.setId(1L);
        user.setEmail("bad_email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        mockMvc.perform(put("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка изменения: пустой емайл")
    void shouldUpdateBadRequestWhenEmailIsInvalid1() throws Exception {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                .content(gson.toJson(initialUser))
                .contentType(MediaType.APPLICATION_JSON));

        User user = new User();
        user.setId(1L);
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        mockMvc.perform(put("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка изменения: пустой логин")
    void shouldUpdateBadRequestWhenLoginIsInvalid() throws Exception {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                .content(gson.toJson(initialUser))
                .contentType(MediaType.APPLICATION_JSON));

        User user = new User();
        user.setId(1L);
        user.setEmail("test@yandex.ru");
        user.setLogin("");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        mockMvc.perform(put("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка изменения: логин с пробелами")
    void shouldUpdateBadRequestWhenLoginIsInvalid1() throws Exception {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                .content(gson.toJson(initialUser))
                .contentType(MediaType.APPLICATION_JSON));

        User user = new User();
        user.setId(1L);
        user.setEmail("test@yandex.ru");
        user.setLogin("log in");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        mockMvc.perform(put("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Изменение пользователя: пустое имя должно заменяться на логин")
    void shouldSetLoginAsNameIfNameIsEmpty1() throws Exception {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                .content(gson.toJson(initialUser))
                .contentType(MediaType.APPLICATION_JSON));

        User user = new User();
        user.setId(1L);
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("");

        mockMvc.perform(put("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("login"))
                .andExpect(jsonPath("$.login").value("login"));
    }

    @Test
    @DisplayName("Ошибка изменения: дата рождения в будущем")
    void shouldUpdateBadRequestWhenBirthdayIsInFuture() throws Exception {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                .content(gson.toJson(initialUser))
                .contentType(MediaType.APPLICATION_JSON));

        User user = new User();
        user.setId(1L);
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));
        user.setName("Name");

        mockMvc.perform(put("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Ошибка изменения: не указан айди")
    void shouldUpdateBadRequestWhenIdIsInFuture() throws Exception {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                .content(gson.toJson(initialUser))
                .contentType(MediaType.APPLICATION_JSON));

        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));
        user.setName("Name");

        mockMvc.perform(put("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Ошибка изменения: не существующий айди")
    void shouldUpdateBadRequestWhenIdIsInFuture1() throws Exception {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                .content(gson.toJson(initialUser))
                .contentType(MediaType.APPLICATION_JSON));

        User user = new User();
        user.setId(10000000000L);
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));
        user.setName("Name");

        mockMvc.perform(put("/users")
                        .content(gson.toJson(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение списка всех пользователей")
    void shouldReturnAllUsers() throws Exception {
        User user1 = new User();
        user1.setEmail("first@yandex.ru");
        user1.setLogin("first_login");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("second@yandex.ru");
        user2.setLogin("second_login");
        user2.setBirthday(LocalDate.of(1995, 5, 5));

        mockMvc.perform(post("/users")
                .content(gson.toJson(user1))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/users")
                .content(gson.toJson(user2))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[1].email").exists());
    }
}