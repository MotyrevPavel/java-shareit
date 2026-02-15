package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.comment.model.Comment;

import java.util.List;
import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
            "WHERE c.item.id = :itemId")
    List<Comment> findCommentsByItemId(@Param("itemId") Long itemId);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.item.id IN :itemIds")
    List<Comment> findCommentsByItemIds(@Param("itemIds") Set<Long> itemIds);

}