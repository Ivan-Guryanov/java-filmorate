package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(of = {"id"})
@Data
public class User {
    private Long id;

    @Email(message = "Введен не имейл")
    @NotBlank(message = "Имейл должен быть указан")
    private String email;

    @NotBlank(message = "Логин должен быть указан")
    private String login;

    private String name;

    @PastOrPresent(message = "Путешественник во времени")
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();

    private Set<Long> initiatedFriendships = new HashSet<>();

    private Set<Long> incomingRequests = new HashSet<>();
}
