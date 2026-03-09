package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Mpa {
    private Integer id;
    private String name;
}
