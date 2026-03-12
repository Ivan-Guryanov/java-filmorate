package ru.yandex.practicum.filmorate.dao.mapper.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    @Builder.Default
    private Set<Long> friends = new HashSet<>();

    @Builder.Default
    private Set<Long> initiatedFriendships = new HashSet<>();

    @Builder.Default
    private Set<Long> incomingRequests = new HashSet<>();
}