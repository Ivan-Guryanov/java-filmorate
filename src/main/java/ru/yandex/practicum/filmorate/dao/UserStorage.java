package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.dao.mapper.user.UserDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<UserDto> findAll();

    UserDto create(User user);

    UserDto update(User newUser);

    UserDto getUsetById(Long id);

}
