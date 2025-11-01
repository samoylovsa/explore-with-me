package ewm.service.user;

import ewm.dto.user.AdminUserGetParams;
import ewm.dto.user.UserDto;
import ewm.exception.NotFoundException;
import ewm.mapper.user.UserMapper;
import ewm.model.user.User;
import ewm.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public List<UserDto> getUsers(AdminUserGetParams params) {
        log.debug("getUsers() with params: ids={}, from={}, size={}",
                params.getIds(), params.getFrom(), params.getSize());

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());

        if (params.getIds() != null && !params.getIds().isEmpty()) {
            return repository.findByIdIn(params.getIds(), pageable)
                    .stream()
                    .map(userMapper::toUserDto)
                    .toList();
        } else {
            return repository.findAll(pageable)
                    .stream()
                    .map(userMapper::toUserDto)
                    .toList();
        }
    }

    @Override
    public void deleteBy(Long userId) {
        log.debug("deleteBy(userId={})", userId);
        User user = repository.findById(userId).orElseThrow(() -> {
            log.debug(repository.findAll().stream().map(User::getId).toList().toString());
            return new NotFoundException("User with id=" + userId + " was not found");
        });

        repository.deleteById(user.getId());
    }
}
