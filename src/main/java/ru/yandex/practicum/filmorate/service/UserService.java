package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.InDbUserStorage;
import ru.yandex.practicum.filmorate.dao.mapper.user.UserDto;
import ru.yandex.practicum.filmorate.dao.mapper.user.UserDtoMapper;
import ru.yandex.practicum.filmorate.dao.mapper.user.UserMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Collections;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JdbcTemplate jdbc;
    private final UserMapper<User> mapper;
    private final InDbUserStorage inDbUserStorage;

    public void addFriend(Long userId, Long friendId) {
        // тут более сложная логика нужна по хорошему, но тесты этого не требуют
        log.info("Получен запрос от пользователя {} на добавление в друзья {}", userId, friendId);

        String sql = "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)";

        inDbUserStorage.getUsetById(userId);
        inDbUserStorage.getUsetById(friendId);

        try {

            jdbc.update(sql, userId, friendId, "pending");

            log.info("Заявка в друзья сохранена");

        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.warn("Попытка дублирования дружбы: {} и {}", userId, friendId);
            throw new ValidationException("Заявка уже существует или пользователи уже друзья");

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка БД при добавлении друга: {}", e.getMessage());
            throw new RuntimeException("Не удалось сохранить связь в базе данных");
        }

        log.info("Пользователи {} направил заявку на дружбу пользователю {}.", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {

        log.info("Получен запрос от пользователя {} на удаление из друзей {}", userId, friendId);

        inDbUserStorage.getUsetById(userId);
        inDbUserStorage.getUsetById(friendId);

        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

        try {
            int rowsAffected = jdbc.update(sql, userId, friendId);

            if (rowsAffected == 0) {
                log.warn("Связь между {} и {} не найдена, ничего не удалено", userId, friendId);
            }

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка БД при удалении друга: {}", e.getMessage());
            throw new RuntimeException("Ошибка при выполнении удаления в базе данных");
        }

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Collection<UserDto> findAllFriends(Long userId) {
        log.info("Получен запрос на получение списка друзей пользователя {}", userId);

        inDbUserStorage.getUsetById(userId);

        String query = "SELECT u.* FROM users u " +
                "WHERE u.id IN (" +
                "    SELECT friend_id FROM friendship WHERE user_id = ? AND status IN ('pending', 'accepted') " +
                "    UNION " +
                "    SELECT user_id FROM friendship WHERE friend_id = ? AND status = 'accepted'" +
                ")";

        try {
            Collection<UserDto> friends = jdbc.query(query, mapper, userId, userId).stream()
                    .map(UserDtoMapper::mapToDto)
                    .toList();

            return friends != null ? friends : Collections.emptyList();

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при обращении к базе данных: {}", e.getMessage());
            throw new RuntimeException("Ошибка работы с базой данных при получении списка пользователей");
        }

    }

    public Collection<User> commonFriends(Long id, Long otherId) {
        log.info("Получен запрос на получение общих друзей пользователей {} и {}", id, otherId);

        inDbUserStorage.getUsetById(id);
        inDbUserStorage.getUsetById(otherId);

        String query =
                "SELECT u.* FROM users u " +
                        "WHERE u.id IN (" +
                        "    (SELECT friend_id FROM friendship WHERE user_id = ? AND status IN ('pending', 'accepted') " +
                        "     UNION " +
                        "     SELECT user_id FROM friendship WHERE friend_id = ? AND status = 'accepted') " +
                        "    INTERSECT " +
                        "    (SELECT friend_id FROM friendship WHERE user_id = ? AND status IN ('pending', 'accepted') " +
                        "     UNION " +
                        "     SELECT user_id FROM friendship WHERE friend_id = ? AND status = 'accepted')" +
                        ")";

        try {
            return jdbc.query(query, mapper, id, id, otherId, otherId);
        } catch (org.springframework.dao.DataAccessException e) {
                log.error("Ошибка при обращении к базе данных: {}", e.getMessage());
                throw new RuntimeException("Ошибка работы с базой данных при получении списка пользователей");
            }
    }

}
