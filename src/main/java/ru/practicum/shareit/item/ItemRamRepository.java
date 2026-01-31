package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ItemRamRepository implements ItemRepository {
    private final Map<Long, Item> itemMap;

    public ItemRamRepository() {
        this.itemMap = new HashMap<>();
    }

    @Override
    public Item create(Item item) {
        Long id = generateId();
        item.setId(id);
        itemMap.put(id, item);
        return item;
    }

    @Override
    public Item update(Item item) {
        Item itemInMap = itemMap.get(item.getId());
        if (itemInMap == null) {
            throw new NotFoundException("Вещи нет в базе по ID " + item.getId());
        }
        itemMap.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getById(Long id) {
        Item itemInMap = itemMap.get(id);
        if (itemInMap == null) {
            throw new NotFoundException("Вещи нет в базе по ID " + id);
        }
        return itemInMap;
    }

    @Override
    public List<Item> getAllByUserId(Long userId) {
        return itemMap.values().stream()
                .filter(item -> Objects.equals(item.getUserId(), userId))
                .toList();
    }

    @Override
    public List<Item> searchAvailableItemByParam(String text) {
        return itemMap.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(Item::getAvailable)
                .toList();
    }

    private Long generateId() {
        Long newId = itemMap.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++newId;
    }
}