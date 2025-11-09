package ewm.controller.comment;

import ewm.dto.comment.CommentDto;
import ewm.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class CommentPublicController {

    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getEventComments(@PathVariable Long eventId) {
        log.info("Public: Retrieving published comments for event ID: {}", eventId);
        List<CommentDto> result = commentService.getPublishedEventComments(eventId);
        log.info("Public: Found {} published comments for event ID: {}", result.size(), eventId);
        return result;
    }

    @GetMapping("/comments/{commentId}")
    public CommentDto getComment(@PathVariable Long commentId) {
        log.info("Public: Retrieving published comment with ID: {}", commentId);
        CommentDto result = commentService.getPublishedComment(commentId);
        log.info("Public: Successfully retrieved published comment with ID: {}", commentId);
        return result;
    }
}
