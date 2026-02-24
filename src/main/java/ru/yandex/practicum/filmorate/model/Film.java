package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(of = {"id"})
@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Длина описания должна быть не более 200 символов")
    private String description;
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть отрицательным числом")
    private int duration;

    private Set<Long> likes = new HashSet<>();

}
