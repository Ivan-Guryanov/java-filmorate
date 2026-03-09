package ru.yandex.practicum.filmorate.twelve;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.InDbUserStorage;
import ru.yandex.practicum.filmorate.dao.mapper.user.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserTest {
    private final InDbUserStorage userStorage;
    private final UserService userService;

    @Test
    @DisplayName("Создание пользователя: валидные данные + получения пользователя по айди")
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        UserDto savedUser = userStorage.create(user);
        long id = savedUser.getId();

        Optional<UserDto> userDtoOptional = Optional.ofNullable(userStorage.getUsetById(id));

        assertThat(userDtoOptional)
                .isPresent()
                .hasValueSatisfying(userDto -> {
                    assertThat(userDto).hasFieldOrPropertyWithValue("id", id);
                    assertThat(userDto).hasFieldOrPropertyWithValue("email", "test@yandex.ru");
                    assertThat(userDto).hasFieldOrPropertyWithValue("login", "login");
                    assertThat(userDto).hasFieldOrPropertyWithValue("name", "Name");

                });
    }


    @Test
    @DisplayName("Изменение пользователя: валидные данные")
    void shouldUpdateCUser() throws Exception {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));
        initialUser.setName("oldName");
        UserDto savedUser = userStorage.create(initialUser);
        Long userId = savedUser.getId();

        User user = new User();
        user.setId(userId);
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        Optional<UserDto> userDtoOptional = Optional.ofNullable(userStorage.update(user));

        assertThat(userDtoOptional)
                .isPresent()
                .hasValueSatisfying(userDto -> {
                    assertThat(userDto).hasFieldOrPropertyWithValue("id", userId);
                    assertThat(userDto).hasFieldOrPropertyWithValue("email", "test@yandex.ru");
                    assertThat(userDto).hasFieldOrPropertyWithValue("login", "login");
                    assertThat(userDto).hasFieldOrPropertyWithValue("name", "Name");

                });
    }

    @Test
    @DisplayName("Получение списка всех пользователей")
    void shouldReturnAllUsers() throws Exception {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));
        initialUser.setName("oldName");
        userStorage.create(initialUser);

        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");
        userStorage.create(user);

        Collection<UserDto> users = userStorage.findAll();

        assertThat(users)
                .isNotNull()
                .hasSize(2)
                .extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("old@yandex.ru", "test@yandex.ru");
    }

    @Test
    @DisplayName("Добавление в друзья: корректные ID")
    void shouldAddFriendCorrectly() {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));
        initialUser.setName("oldName");
        UserDto savedUser1 = userStorage.create(initialUser);
        Long userId = savedUser1.getId();

        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");
        UserDto savedUser2 = userStorage.create(user);
        Long friendId = savedUser2.getId();

        userService.addFriend(userId, friendId);

        Collection<UserDto> friends = userService.findAllFriends(userId);

        assertThat(friends)
                .isNotNull()
                .extracting(UserDto::getId)
                .contains(friendId);
    }

    @Test
    @DisplayName("Удаление из друзей: корректные ID")
    void shoulDeleteFriendCorrectly() {
        User initialUser = new User();
        initialUser.setEmail("old@yandex.ru");
        initialUser.setLogin("old_login");
        initialUser.setBirthday(LocalDate.of(2000, 1, 1));
        initialUser.setName("oldName");
        UserDto savedUser1 = userStorage.create(initialUser);
        Long userId = savedUser1.getId();

        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");
        UserDto savedUser2 = userStorage.create(user);
        Long friendId = savedUser2.getId();

        userService.addFriend(userId, friendId);

        userService.deleteFriend(userId, friendId);

        Collection<UserDto> friends = userService.findAllFriends(userId);

        assertThat(friends)
                .isNotNull()
                .extracting(UserDto::getId)
                .doesNotContain(friendId);
    }

    @Test
    @DisplayName("Друзья: получение списка друзей и поиск общих")
    void shouldReturnFriendsAndCommonFriends() {

        User user1 = new User();
        user1.setEmail("user1@test.ru");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));
        Long id1 = userStorage.create(user1).getId();

        User user2 = new User();
        user2.setEmail("user2@test.ru");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        Long id2 = userStorage.create(user2).getId();

        User user3 = new User();
        user3.setEmail("user3@test.ru");
        user3.setLogin("user3");
        user3.setBirthday(LocalDate.of(2000, 1, 1));
        Long id3 = userStorage.create(user3).getId();

        userService.addFriend(id1, id3);
        userService.addFriend(id2, id3);

        Collection<UserDto> friends1 = userService.findAllFriends(id1);
        assertThat(friends1)
                .isNotNull()
                .hasSize(1)
                .extracting(UserDto::getId)
                .containsExactly(id3);

        Collection<User> common = userService.commonFriends(id1, id2);
        assertThat(common)
                .isNotNull()
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(id3);
    }
}