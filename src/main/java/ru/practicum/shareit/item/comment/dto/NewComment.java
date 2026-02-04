package ru.practicum.shareit.item.comment.dto;

import lombok.Data;
import lombok.NonNull;
import ru.practicum.shareit.validator.ConditionalNotBlank;

@Data
public class NewComment {
    @NonNull
    @ConditionalNotBlank
    private String text;

    public NewComment() {
    }
}
