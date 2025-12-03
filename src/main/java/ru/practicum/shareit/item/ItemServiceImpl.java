package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(NewItem newItem, Long userId) {
        log.info("Попытка добавления новой вещи {}", newItem);
        if (!userRepository.isUserExist(userId)) {
            log.error("В базе отсутствует пользователь с ID {}", userId);
            throw new NotFoundException("В базе отсутствует пользователь с ID " + userId);
        }
        Item item = ItemMapper.toItem(newItem, userId);
        item = itemRepository.create(item);
        log.info("Вещь добавлена в репозиторий {}", item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto update(UpdateItem updateItem, Long itemId, Long userId) {
        log.info("Попытка обновления вещи {}", updateItem);
        Item item = itemRepository.getById(itemId);
        log.info("Получили по ID {} из репозитория вещь {}", itemId, item);
        if (!Objects.equals(item.getUserId(), userId)) {
            log.error("Попытка обновления пользователя с ID {} информации о вещи владельца с ID {}",
                    item.getUserId(), userId);
            throw new NotFoundException("Обновлять информацию может только владелец вещи");
        }
        item = ItemMapper.toItem(item, updateItem);
        item = itemRepository.update(item);
        log.info("Обновили данные о вещи в БД {}", item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getById(Long id) {
        log.info("Попытка получения вещи по {}", id);
        Item item = itemRepository.getById(id);
        log.info("Вещь по ID {} получена {}", id, item);
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getAllByUserId(Long userId) {
        log.info("Попытка получения всеъ вещей по пользователю с ID {}", userId);
        List<Item> items = itemRepository.getAllByUserId(userId);
        log.info("Получен по пользователю с ID {} список вещей {}", userId, items);
        return items.stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchAvailableItemByParam(String text) {
        log.info("Попытка получения всех вещей по параметру {}", text);
        if (text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.searchAvailableItemByParam(text);
        log.info("Получен по параметру{} список вещей {}", text, items);
        return items.stream()
                .map(ItemMapper::toDto)
                .toList();
    }
}