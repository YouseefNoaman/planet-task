package planettask.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import planettask.domain.User;
import planettask.model.UserDTO;
import planettask.repos.UserRepository;
import planettask.util.NotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private UserService userService;

  private User user;
  private UserDTO userDTO;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setUserId(1L);
    user.setEmail("test@example.com");

    userDTO = new UserDTO();
    userDTO.setUserId(1L);
    userDTO.setEmail("test@example.com");
  }

  @Test
  void findAll_ShouldReturnUserDTOList_WhenUsersExist() {
    Pageable pageable = PageRequest.of(0, 10);
    List<User> userList = List.of(user);
    Page<User> userPage = new PageImpl<>(userList);

    when(userRepository.findAll(pageable)).thenReturn(userPage);
    when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

    List<UserDTO> result = userService.findAll(pageable);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(userDTO.getEmail(), result.getFirst().getEmail());

    verify(userRepository, times(1)).findAll(pageable);
  }

  @Test
  void get_ShouldReturnUserDTO_WhenUserExists() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

    UserDTO result = userService.get(1L);

    assertNotNull(result);
    assertEquals(userDTO.getUserId(), result.getUserId());
    assertEquals(userDTO.getEmail(), result.getEmail());

    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  void get_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.get(1L));

    verify(userRepository, times(1)).findById(1L);
    verifyNoMoreInteractions(modelMapper);
  }

  @Test
  void create_ShouldReturnUserId_WhenUserIsCreatedSuccessfully() {
    when(userRepository.existsByEmailIgnoreCase(userDTO.getEmail())).thenReturn(false);
    when(modelMapper.map(userDTO, User.class)).thenReturn(user);
    when(userRepository.save(user)).thenReturn(user);

    Long result = userService.create(userDTO);

    assertNotNull(result);
    assertEquals(user.getUserId(), result);

    verify(userRepository, times(1)).existsByEmailIgnoreCase(userDTO.getEmail());
    verify(userRepository, times(1)).save(user);
  }

  @Test
  void create_ShouldThrowException_WhenEmailAlreadyExists() {
    when(userRepository.existsByEmailIgnoreCase(userDTO.getEmail())).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> userService.create(userDTO));

    verify(userRepository, times(1)).existsByEmailIgnoreCase(userDTO.getEmail());
    verifyNoMoreInteractions(userRepository);
  }
}
