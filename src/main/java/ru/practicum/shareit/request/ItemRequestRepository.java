package ru.practicum.shareit.request;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByUserId(Long userId, Sort sort);

    List<ItemRequest> findAllByUser_IdNot(Long userId, Sort sort);
}
