package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dao.InDbUserStorage;
import ru.yandex.practicum.filmorate.dao.mapper.user.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final InDbUserStorage inDbUserStorage;
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAll() {
        return inDbUserStorage.findAll();
    }

    @GetMapping("/{id}")
    public UserDto getUsetById(@PathVariable Long id) {
        return inDbUserStorage.getUsetById(id);
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody User user) {
        return inDbUserStorage.create(user);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody User newUser) {
        return inDbUserStorage.update(newUser);
    }

    @PutMapping ("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping ("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.deleteFriend(id, friendId);
    }

    @GetMapping ("/{id}/friends")
    public Collection<UserDto> findAllFriends(@PathVariable Long id) {
        return userService.findAllFriends(id);
    }

    @GetMapping ("/{id}/friends/common/{otherId}")
    public Collection<User> commonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.commonFriends(id, otherId);
    }
}
