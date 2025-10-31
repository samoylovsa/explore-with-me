package ewm.mapper.user;

import ewm.dto.user.UserDto;
import ewm.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toUserDto(User user);

    User toUser(UserDto userShortDto);
}
