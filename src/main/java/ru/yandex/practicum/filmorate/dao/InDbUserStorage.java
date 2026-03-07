package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mapper.user.UserDto;
import ru.yandex.practicum.filmorate.dao.mapper.user.UserDtoMapper;
import ru.yandex.practicum.filmorate.dao.mapper.user.UserMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class InDbUserStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserMapper<User> mapper;

    @Override
    public Collection<UserDto> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        String query = "SELECT * FROM users LIMIT 100";
        try {
            return jdbc.query(query, mapper).stream()
                    .map(UserDtoMapper::mapToDto)
                    .toList();
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при обращении к базе данных: {}", e.getMessage());
            throw new RuntimeException("Ошибка работы с базой данных при получении списка пользователей");
        }
    }

    @Override
    public UserDto create(User user) {
        log.info("Получен запрос на создание пользователя");

        if (user.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы");
            throw new ValidationException("Логин не должен содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setObject(4, user.getBirthday());
                return ps;
            }, keyHolder);

            Long id = keyHolder.getKeyAs(Long.class);
            user.setId(id);

            log.info("Пользователь сохранен в БД с id: {}", id);

            return UserDtoMapper.mapToDto(user);

        } catch (org.springframework.dao.DuplicateKeyException e) {
            String errorMessage = e.getMessage().toUpperCase();
            log.error("Ошибка дублирования данных: {}", errorMessage);

            if (errorMessage.contains("EMAIL")) {
                throw new ValidationException("Пользователь с таким email уже зарегистрирован");
            }

            if (errorMessage.contains("LOGIN")) {
                throw new ValidationException("Логин уже занят другим пользователем");
            }

            throw new ValidationException("Данные пользователя (email или login) уже существуют в системе");
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при работе с БД: {}", e.getMessage());
            throw new RuntimeException("Ошибка сохранения пользователя");
        }
    }

    @Override
    public UserDto update(User newUser) {
        log.info("Получен запрос на изменение пользователя");


        if (newUser.getId() == null) {
            log.error("Не указан айди пользователя");
            throw new ValidationException("Id должен быть указан");
        }

        if (newUser.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы");
            throw new ValidationException("Логин не должен содержать пробелы");
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        try {
            int rowsAffected = jdbc.update(sql,
                    newUser.getEmail(),
                    newUser.getLogin(),
                    newUser.getName(),
                    newUser.getBirthday(),
                    newUser.getId());

            if (rowsAffected == 0) {
                log.error("Пользователь с id {} не найден", newUser.getId());
                throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
            }

            log.info("Пользователь с id {} успешно обновлен", newUser.getId());

            return UserDtoMapper.mapToDto(newUser);

        } catch (org.springframework.dao.DuplicateKeyException e) {
            String errorMessage = e.getMessage().toUpperCase();
            log.error("Ошибка дублирования данных при обновлении: {}", errorMessage);

            if (errorMessage.contains("EMAIL")) {
                throw new ValidationException("Этот email уже занят другим пользователем");
            }
            if (errorMessage.contains("LOGIN")) {
                throw new ValidationException("Этот логин уже занят другим пользователем");
            }
            throw new ValidationException("Ошибка уникальности данных");

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при работе с БД при обновлении id {}: {}", newUser.getId(), e.getMessage());
            throw new RuntimeException("Ошибка обновления пользователя");
        }

    }

    @Override
    public UserDto getUsetById(Long id) {
        log.info("Получен запрос на получение пользователя по id{}", id);
        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            return UserDtoMapper.mapToDto(jdbc.queryForObject(sql, mapper, id));
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.error("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id " + id + " не существует");
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Ошибка при выполнении запроса к БД: {}", e.getMessage());
            throw new RuntimeException("Ошибка базы данных");
        }
    }

}
