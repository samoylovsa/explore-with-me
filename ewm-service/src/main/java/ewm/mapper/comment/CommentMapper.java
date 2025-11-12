package ewm.mapper.comment;

import ewm.dto.comment.CommentDto;
import ewm.dto.comment.NewCommentDto;
import ewm.model.comment.Comment;
import ewm.model.event.Event;
import ewm.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.name")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "text", source = "newCommentDto.text")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "author", source = "author")
    Comment toEntity(NewCommentDto newCommentDto, Event event, User author);
}
