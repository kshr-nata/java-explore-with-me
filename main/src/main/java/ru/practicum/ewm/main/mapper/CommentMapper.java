package ru.practicum.ewm.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.dto.CommentDto;
import ru.practicum.ewm.main.dto.RequestCommentDto;
import ru.practicum.ewm.main.model.Comment;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {
    public static Comment mapToComment(User user, Event event, RequestCommentDto dto) {
        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setText(dto.getText());
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    public static CommentDto mapToDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getEvent().getId(),
                comment.getAuthor().getId(), comment.getText(), comment.getCreated());
    }
}
