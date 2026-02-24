package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUser;
import ru.practicum.shareit.user.dto.UpdateUser;
import ru.practicum.shareit.user.dto.UserDto;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody NewUser user) {
        return ResponseEntity.ok().body(userService.create(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok().body(userService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(@RequestBody UpdateUser user, @PathVariable Long id) {
        return ResponseEntity.ok().body(userService.update(user, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}