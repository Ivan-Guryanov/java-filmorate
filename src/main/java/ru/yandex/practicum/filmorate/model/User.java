package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(of = {"id"})
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Введен не имейл")
    private String email;

    @NotBlank(message = "Логин должен быть указан")
    private String login;

    private String name;

    @PastOrPresent(message = "Путешественник во времени")
    private LocalDate birthday;

    @Builder.Default
    private Set<Long> friends = new HashSet<>();

    @Builder.Default
    private Set<Long> initiatedFriendships = new HashSet<>();

    @Builder.Default
    private Set<Long> incomingRequests = new HashSet<>();
}
