package ewm.service.user;

import ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    List<UserDto> getUsers();

    void deleteBy(Long userId);
}
