package ewm.service.comment;


import ewm.dto.comment.CommentDto;
import ewm.dto.comment.CommentStatusUpdateDto;
import ewm.dto.comment.NewCommentDto;
import ewm.dto.comment.UpdateCommentDto;
import ewm.model.comment.CommentStatus;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    void deleteCommentByOwner(Long userId, Long commentId);

    List<CommentDto> getOwnerComments(Long userId);

    CommentDto moderateComment(Long commentId, CommentStatusUpdateDto updateDto);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDto> getCommentsForAdmin(CommentStatus status, Long eventId, Long userId);

    List<CommentDto> getPublishedEventComments(Long eventId);

    CommentDto getPublishedComment(Long commentId);
}
