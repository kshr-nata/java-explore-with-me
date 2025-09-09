package ru.practicum.ewm.main.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CommentDto;
import ru.practicum.ewm.main.dto.RequestCommentDto;
import ru.practicum.ewm.main.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @Autowired
    public PrivateCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public CommentDto addComment(@PathVariable Long userId,
                                 @RequestParam Long eventId,
                                 @Valid @RequestBody RequestCommentDto dto) {
        return commentService.addComment(userId, eventId, dto);
    }

    @GetMapping
    List<CommentDto> getCommentsByUser(@PathVariable Long userId) {
        return commentService.getCommentsByUser(userId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @Valid @RequestBody RequestCommentDto dto) {
        return commentService.updateCommentByUser(userId, commentId, dto);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        commentService.removeComment(userId, commentId);
    }
}
