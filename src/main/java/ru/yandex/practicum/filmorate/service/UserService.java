package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public void addFriend(Long userId, Long friendId) {
        log.info("Получен запрос от пользователя {} на добавление в друзья {}", userId, friendId);

        User user = userStorage.getUsetById(userId);
        User friend = userStorage.getUsetById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Получен запрос от пользователя {} на удаление из друзей {}", userId, friendId);

        User user = userStorage.getUsetById(userId);
        User friend = userStorage.getUsetById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Пользователи {} и {} теперь не друзья", userId, friendId);
    }

    public Collection<User> findAllFriends(Long userId) {
        log.info("Получен запрос на получение списка друзей пользователя {}", userId);

        User user = userStorage.getUsetById(userId);

        return user.getFriends().stream()
                .map(userStorage::getUsetById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Collection<User> commonFriends(Long id, Long otherId) {
        log.info("Получен запрос на получение общих друзей пользователей {} и {}", id, otherId);

        User user = userStorage.getUsetById(id);
        User otherUser = userStorage.getUsetById(otherId);

        return userStorage.getUsetById(id).getFriends().stream()
                .filter(userStorage.getUsetById(otherId).getFriends()::contains)
                .map(userStorage::getUsetById)
                .collect(Collectors.toList());
    }

}
