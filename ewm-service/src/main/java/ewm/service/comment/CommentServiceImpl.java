package ewm.service.comment;

import ewm.dto.comment.CommentDto;
import ewm.dto.comment.CommentStatusUpdateDto;
import ewm.dto.comment.NewCommentDto;
import ewm.dto.comment.UpdateCommentDto;
import ewm.exception.AccessDeniedException;
import ewm.exception.NotFoundException;
import ewm.mapper.comment.CommentMapper;
import ewm.model.comment.Comment;
import ewm.model.comment.CommentStatus;
import ewm.model.event.Event;
import ewm.model.user.User;
import ewm.repository.comment.CommentRepository;
import ewm.repository.event.EventRepository;
import ewm.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto createComment(Long userId, NewCommentDto newCommentDto) {
        User author = getUser(userId);
        Event event = getEvent(newCommentDto.getEventId());
        Comment comment = Comment.builder()
                .text(newCommentDto.getText())
                .event(event)
                .author(author)
                .status(CommentStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = getComment(commentId);
        validateCommentOwner(comment, userId);
        validateCommentCanBeUpdated(comment);
        comment.setText(updateCommentDto.getText());
        comment.setUpdated(LocalDateTime.now());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentByOwner(Long userId, Long commentId) {
        Comment comment = getComment(commentId);
        validateCommentOwner(comment, userId);
        if (comment.getStatus() != CommentStatus.DELETED) {
            comment.setStatus(CommentStatus.DELETED);
            comment.setUpdated(LocalDateTime.now());
            commentRepository.save(comment);
        }
    }

    @Override
    public List<CommentDto> getOwnerComments(Long userId) {
        getUser(userId);
        return commentRepository.findByAuthorId(userId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto moderateComment(Long commentId, CommentStatusUpdateDto updateDto) {
        Comment comment = getComment(commentId);
        validateCommentCanBeModerated(comment);
        validateModerationStatus(updateDto.getStatus());
        comment.setStatus(updateDto.getStatus());
        comment.setUpdated(LocalDateTime.now());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = getComment(commentId);
        if (comment.getStatus() != CommentStatus.DELETED) {
            comment.setStatus(CommentStatus.DELETED);
            comment.setUpdated(LocalDateTime.now());
            commentRepository.save(comment);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForAdmin(CommentStatus status, Long eventId, Long userId) {
        return commentRepository.findCommentsByFilters(status, eventId, userId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getPublishedEventComments(Long eventId) {
        getEvent(eventId);
        return commentRepository.findByEventIdAndStatus(eventId, CommentStatus.PUBLISHED).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getPublishedComment(Long commentId) {
        Comment comment = commentRepository.findByIdAndStatus(commentId, CommentStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Comment not found or not published"));
        return commentMapper.toDto(comment);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));
    }

    private void validateCommentOwner(Comment comment, Long userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("User with id=" + userId + " is not the owner of comment with id=" + comment.getId());
        }
    }

    private void validateCommentCanBeUpdated(Comment comment) {
        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new IllegalStateException("Can only update PENDING comments. Current status: " + comment.getStatus());
        }
    }

    private void validateCommentCanBeModerated(Comment comment) {
        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new IllegalStateException("Can only moderate PENDING comments. Current status: " + comment.getStatus());
        }
    }

    private void validateModerationStatus(CommentStatus status) {
        if (status != CommentStatus.PUBLISHED && status != CommentStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid status for moderation. Only PUBLISHED or REJECTED allowed. Received: " + status);
        }
    }
}
