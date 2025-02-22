package planettask.service;

import jakarta.transaction.Transactional;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import planettask.domain.User;
import planettask.model.UserDTO;
import planettask.repos.UserRepository;
import planettask.util.NotFoundException;


@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final ModelMapper modelMapper;

  public UserService(final UserRepository userRepository, ModelMapper modelMapper) {
    this.userRepository = userRepository;
    this.modelMapper = modelMapper;
  }

  @Cacheable(value = "user", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
  public List<UserDTO> findAll(final Pageable pageable) {
    final Page<User> users = this.userRepository.findAll(pageable);
    return users.stream()
        .map(user -> this.modelMapper.map(user, UserDTO.class))
        .toList();
  }


  @Cacheable(value = "user", key = "#userId")
  public UserDTO get(final Long userId) {
    return userRepository.findById(userId)
        .map(user -> modelMapper.map(user, UserDTO.class))
        .orElseThrow(NotFoundException::new);
  }

  public Long create(final UserDTO userDTO) {
    if (userRepository.existsByEmailIgnoreCase(userDTO.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }
    User user = modelMapper.map(userDTO, User.class);
    return userRepository.save(user).getUserId();
  }
}
