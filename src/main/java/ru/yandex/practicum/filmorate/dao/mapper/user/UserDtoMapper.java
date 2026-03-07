package ru.yandex.practicum.filmorate.dao.mapper.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDtoMapper<U> {

    public static UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .friends(new HashSet<>())
                .initiatedFriendships(new HashSet<>())
                .incomingRequests(new HashSet<>())
                .build();
    }

}