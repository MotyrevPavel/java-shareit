package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto create(Long userId, NewItemRequest newItemRequest) {
        log.info("Создание нового запроса от пользователя с ID {}", userId);
        User user = findUserById(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(newItemRequest, user);
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(savedItemRequest);
        log.info("Создан запрос {} от пользователя с ID {}", itemRequestDto, userId);
        return itemRequestDto;
    }

    @Override
    public List<ItemRequestWithAnswersDto> getAllByUserId(Long userId) {
        log.info("Попытка получения всех запросов пользователя с ID {}", userId);
        findUserById(userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> itemRequestList = itemRequestRepository.findAllByUserId(userId, sort);
        List<ItemRequestWithAnswersDto> itemRequestWithAnswersDtoList = itemRequestList.stream()
                .map(ItemRequestMapper::toItemRequestDtoWithAnswers)
                .toList();
        log.info("Получен список запросов {} пользователя с ID {}", itemRequestWithAnswersDtoList, userId);
        return itemRequestWithAnswersDtoList;
    }

    @Override
    public List<ItemRequestDto> getAllRequestsAnotherUsers(Long userId) {
        log.info("Попытка получения всех запросов от пользователя с ID {}", userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> itemRequestList = itemRequestRepository.findAllByUser_IdNot(userId, sort);
        List<ItemRequestDto> itemRequestDtoList = itemRequestList.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
        log.info("Получен список запросов {}", itemRequestDtoList);
        return itemRequestDtoList;
    }

    @Override
    public ItemRequestWithAnswersDto findById(Long requestId) {
        log.info("Попытка найти запрос по ID {}", requestId);
        Optional<ItemRequest> optionalItemRequest = itemRequestRepository.findById(requestId);
        if (optionalItemRequest.isEmpty()) {
            log.error("Не найдет запрос по ID {}", requestId);
            throw new NotFoundException("не найден запрос по ID " + requestId);
        }
        ItemRequest itemRequest = optionalItemRequest.get();
        ItemRequestWithAnswersDto itemRequestWithAnswersDto =
                ItemRequestMapper.toItemRequestDtoWithAnswers(itemRequest);
        log.info("Найден запрос по ID {}", itemRequestWithAnswersDto);
        return itemRequestWithAnswersDto;
    }

    private User findUserById(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.error("Отсутствует пользователь по ID {}", userId);
            throw new NotFoundException("Отсутствует пользователь по ID " + userId);
        }
        return optionalUser.get();
    }
}
