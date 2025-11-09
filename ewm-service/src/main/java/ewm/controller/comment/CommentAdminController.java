package ewm.controller.comment;

import ewm.dto.comment.CommentDto;
import ewm.dto.comment.CommentStatusUpdateDto;
import ewm.model.comment.CommentStatus;
import ewm.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class CommentAdminController {

    private final CommentService commentService;

    @PatchMapping("/{commentId}")
    public CommentDto moderateComment(@PathVariable Long commentId,
                                      @RequestBody CommentStatusUpdateDto updateDto) {
        log.info("Admin: Moderating comment with ID: {}, new status: {}", commentId, updateDto.getStatus());
        CommentDto result = commentService.moderateComment(commentId, updateDto);
        log.info("Admin: Successfully moderated comment with ID: {}, new status: {}", commentId, result.getStatus());
        return result;
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        log.info("Admin: Deleting comment with ID: {}", commentId);
        commentService.deleteCommentByAdmin(commentId);
        log.info("Admin: Successfully deleted comment with ID: {}", commentId);
    }

    @GetMapping
    public List<CommentDto> getComments(@RequestParam(required = false) CommentStatus status,
                                        @RequestParam(required = false) Long eventId,
                                        @RequestParam(required = false) Long userId) {
        log.info("Admin: Searching comments with filters - status: {}, eventId: {}, userId: {}", status, eventId, userId);
        List<CommentDto> result = commentService.getCommentsForAdmin(status, eventId, userId);
        log.info("Admin: Found {} comments with specified filters", result.size());
        return result;
    }
}
