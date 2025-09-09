package ru.practicum.ewm.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.dto.CommentDto;
import ru.practicum.ewm.main.dto.RequestCommentDto;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.mapper.CommentMapper;
import ru.practicum.ewm.main.model.Comment;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.model.User;
import ru.practicum.ewm.main.repository.CommentRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.UserRepository;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.model.Event;

import java.util.List;

@Service
public class CommentService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(UserRepository userRepository, EventRepository eventRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.commentRepository = commentRepository;
    }

    public CommentDto addComment(Long userId, Long eventId, RequestCommentDto requestCommentDto) {
        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).orElseThrow(()
                -> new NotFoundException("Событие с id=" + eventId + " не найдено или не опубликовано"));
        Comment comment = CommentMapper.mapToComment(author, event, requestCommentDto);
        commentRepository.save(comment);
        return CommentMapper.mapToDto(comment);
    }

    public CommentDto updateCommentByUser(Long userId, Long commentId, RequestCommentDto dto) {
        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment with id " + commentId + " not found"));
        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new ConflictException("Пользователь с id " + userId + " не является автором комментария");
        }
        comment.setText(dto.getText());
        commentRepository.save(comment);
        return CommentMapper.mapToDto(comment);
    }

    public List<CommentDto> getCommentsByUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        List<Comment> comments = commentRepository.findAllByAuthorId(userId);
        return comments.stream()
                .map(CommentMapper::mapToDto).toList();
    }

    public void removeComment(Long userId, Long commentId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment with id " + commentId + " not found"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Удалить комментарий может только его автор");
        }
        commentRepository.deleteById(commentId);
    }

    public List<CommentDto> getCommentsByEvent(Long eventId) {
        eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        return comments.stream()
                .map(CommentMapper::mapToDto).toList();
    }

    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()
                -> new NotFoundException("Comment with id " + commentId + " not found"));
        commentRepository.deleteById(commentId);
    }
}
