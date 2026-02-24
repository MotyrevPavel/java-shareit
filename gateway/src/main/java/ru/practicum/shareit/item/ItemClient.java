package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.comment.dto.NewComment;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(NewItemDto itemDto, Long userId) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(UpdateItemDto itemDto, Long itemId, Long userId) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getById(Long itemId) {
        return get("/" + itemId);
    }

    public ResponseEntity<Object> getByItemsByUserId(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> searchAvailableItemByParam(String text) {
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get("/search?text={text}", null, parameters);
    }

    public ResponseEntity<Object> createNewComment(Long itemId, Long userId, NewComment comment) {
        return post("/" + itemId + "/comment", userId, comment);
    }
}