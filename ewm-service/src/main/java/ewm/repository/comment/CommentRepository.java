package ewm.repository.comment;

import ewm.model.comment.Comment;
import ewm.model.comment.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByAuthorId(Long authorId);

    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status);

    Optional<Comment> findByIdAndStatus(Long id, CommentStatus status);

    @Query("SELECT c FROM Comment c " +
            "WHERE (:status IS NULL OR c.status = :status) " +
            "AND (:eventId IS NULL OR c.event.id = :eventId) " +
            "AND (:userId IS NULL OR c.author.id = :userId)")
    List<Comment> findCommentsByFilters(@Param("status") CommentStatus status,
                                        @Param("eventId") Long eventId,
                                        @Param("userId") Long userId);
}
