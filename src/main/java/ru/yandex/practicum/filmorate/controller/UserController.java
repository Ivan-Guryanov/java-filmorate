package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {

        log.info("Получен запрос на создание пользователя");

        boolean isEmailTaken = users.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));

        if (isEmailTaken) {
            log.error("Указан ранее использованный имейл");
            throw new ValidationException("Этот имейл уже используется");
        }

        if (user.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы");
            throw new ValidationException("Логин не должен содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        log.info("Добавлен пользователь" + user.getId());

        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {

        log.info("Получен запрос на изменение пользователя");

        User oldUser = users.get(newUser.getId());

        if (newUser.getId() == null) {
            log.error("Не указан айди пользователя");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.error("Указан не существующий айди");
            throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        if (!newUser.getEmail().equals(oldUser.getEmail())) {
            boolean emailExists = users.values().stream()
                    .anyMatch(u -> u.getEmail().equals(newUser.getEmail()));
            if (emailExists) {
                log.error("Указан занятый емайл");
                throw new ValidationException("Этот имейл уже занят");
            }
        }

        if (newUser.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы");
            throw new ValidationException("Логин не должен содержать пробелы");
        }

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        users.put(newUser.getId(), newUser);
        return newUser;

    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
