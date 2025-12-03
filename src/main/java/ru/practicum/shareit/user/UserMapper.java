package ru.practicum.shareit.user;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.NewUser;
import ru.practicum.shareit.user.dto.UpdateUser;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class UserMapper {
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(NewUser newUser) {
        return User.builder()
                .name(newUser.getName())
                .email(newUser.getEmail())
                .build();
    }

    public static User toUser(User user, UpdateUser updateUser) {
        if (updateUser.hasName()) {
            user.setName(updateUser.getName());
        }
        if (updateUser.hasEmail()) {
            user.setEmail(updateUser.getEmail());
        }
        return user;
    }
}