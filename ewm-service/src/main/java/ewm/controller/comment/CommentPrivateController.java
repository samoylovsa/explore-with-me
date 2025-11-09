package ewm.controller.comment;

import ewm.dto.comment.CommentDto;
import ewm.dto.comment.NewCommentDto;
import ewm.dto.comment.UpdateCommentDto;
import ewm.service.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class CommentPrivateController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId,
                                    @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("User {}: Creating new comment for event {}", userId, newCommentDto.getEventId());
        CommentDto result = commentService.createComment(userId, newCommentDto);
        log.info("User {}: Successfully created comment with ID: {}", userId, result.getId());
        return result;
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("User {}: Updating comment with ID: {}", userId, commentId);
        CommentDto result = commentService.updateComment(userId, commentId, updateCommentDto);
        log.info("User {}: Successfully updated comment with ID: {}", userId, commentId);
        return result;
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("User {}: Deleting comment with ID: {}", userId, commentId);
        commentService.deleteCommentByOwner(userId, commentId);
        log.info("User {}: Successfully deleted comment with ID: {}", userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getOwnerComments(@PathVariable Long userId) {
        log.info("User {}: Retrieving all user comments", userId);
        List<CommentDto> result = commentService.getOwnerComments(userId);
        log.info("User {}: Found {} comments", userId, result.size());
        return result;
    }
}