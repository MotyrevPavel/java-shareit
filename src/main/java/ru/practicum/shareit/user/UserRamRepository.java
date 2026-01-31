package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class UserRamRepository implements UserRepository {
    private final Map<Long, User> userMap;
    private final Set<String> emailSet;

    public UserRamRepository() {
        this.userMap = new HashMap<>();
        this.emailSet = new HashSet<>();
    }

    @Override
    public User create(User user) {
        Long id = generateId();
        User userCopy = copyUser(user);
        userCopy.setId(id);
        userMap.put(id, userCopy);
        emailSet.add(userCopy.getEmail());
        user.setId(id);
        return user;
    }

    @Override
    public User getById(Long id) {
        User user = userMap.get(id);
        return copyUser(user);
    }

    @Override
    public User update(User user) {
        User oldUser = userMap.get(user.getId());
        emailSet.remove(oldUser.getEmail());
        User updatedUserCopy = copyUser(user);
        emailSet.add(updatedUserCopy.getEmail());
        userMap.put(updatedUserCopy.getId(), updatedUserCopy);
        return user;
    }

    @Override
    public void delete(Long id) {
        User user = userMap.get(id);
        emailSet.remove(user.getEmail());
        userMap.remove(id);
    }

    @Override
    public boolean emailIsNotAvailable(String email) {
        return emailSet.contains(email);
    }

    private Long generateId() {
        long newId = userMap.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++newId;
    }

    @Override
    public boolean isUserExist(Long userId) {
        return userMap.containsKey(userId);
    }

    private User copyUser(User user) {
        return User.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail()).build();
    }
}