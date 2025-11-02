package ewm.service.user;

import ewm.dto.user.AdminUserGetParams;
import ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    List<UserDto> getUsers(AdminUserGetParams params);

    void deleteBy(Long userId);

    UserDto getUserById(Long userId);
}
