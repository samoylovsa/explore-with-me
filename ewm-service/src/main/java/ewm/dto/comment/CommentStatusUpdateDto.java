package ewm.dto.comment;

import ewm.model.comment.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentStatusUpdateDto {
    private CommentStatus status;
}
