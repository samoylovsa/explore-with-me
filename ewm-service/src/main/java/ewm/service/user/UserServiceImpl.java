package ewm.service.user;

import ewm.dto.user.UserDto;
import ewm.mapper.user.UserMapper;
import ewm.model.user.User;
import ewm.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.debug("createUser(userDto={})", userDto);

        User user = repository.save(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        log.debug("getUsers()");

        return repository.findAll()
                .stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteBy(Long userId) {
        log.debug("deleteBy(userId={})", userId);

        repository.deleteById(userId);
    }
}
