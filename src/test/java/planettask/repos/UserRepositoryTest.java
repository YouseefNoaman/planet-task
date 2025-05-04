package planettask.repos;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;
import planettask.domain.User;

@DataJpaTest
@AutoConfigureTestEntityManager
@Slf4j
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;
  private User user;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    user = new User();
    user.setUsername("Test User");
    user.setEmail("test@example.com");
    userRepository.save(user);
  }

  @Test
  public void testExistsByEmailIgnoreCase_UserExists() {
    // Given

    // When
    boolean exists = userRepository.existsByEmailIgnoreCase("TEST@EXAMPLE.COM");

    // Then
    assertTrue(exists, "User should exist even if email case is different.");
  }

  @Test
  public void testExistsByEmailIgnoreCase_UserDoesNotExist() {
    // When
    boolean exists = userRepository.existsByEmailIgnoreCase("notfound@example.com");

    // Then
    assertFalse(exists, "User should not exist in database.");
  }

  @Test
  public void testFindById_UserExists() {
    // Given
    User user = new User();
    user.setEmail("found@example.com");
    user.setUsername("Found User");
    userRepository.save(user);
    // When
    Optional<User> foundUser = userRepository.findById(user.getUserId());

    // Then
    assertTrue(foundUser.isPresent(), "User should be found by ID.");
    log.info("User email: {}", user.getEmail());
    log.info("Found user email: {}", foundUser.get().getEmail());
    assertEquals(user.getEmail(),  foundUser.get().getEmail(),"Emails should match.");
  }

  @Test
  public void testFindById_UserDoesNotExist() {
    // When
    Optional<User> foundUser = userRepository.findById(999L);

    // Then
    assertFalse(foundUser.isPresent(), "No user should be found for a non-existing ID.");
  }

  @Test
  public void testExistsByEmailIgnoreCase_EmptyEmail() {
    boolean exists = userRepository.existsByEmailIgnoreCase("");

    assertFalse(exists, "Empty email should not be found.");
  }

  @Test
  public void testExistsByEmailIgnoreCase_NullEmail() {
    boolean exists = userRepository.existsByEmailIgnoreCase(null);

    assertFalse(exists, "Null email should not be found.");
  }

  @Test
  public void testExistsByEmailIgnoreCase_WhitespaceEmail() {
    User user = new User();
    user.setEmail("trimmed@example.com");
    user.setUsername("Trimmed User");
    userRepository.save(user);

    boolean exists = userRepository.existsByEmailIgnoreCase("   trimmed@example.com   ");

    assertFalse(exists, "Whitespace should not match email.");
  }

  @Test
  public void testExistsByEmailIgnoreCase_PartialEmail() {
    User user = new User();
    user.setEmail("partial@example.com");
    user.setUsername("Partial User");
    userRepository.save(user);

    boolean exists = userRepository.existsByEmailIgnoreCase("partial@");
    boolean exists2 = userRepository.existsByEmailIgnoreCase("example.com");

    assertFalse(exists, "Partial email should not be found.");
    assertFalse(exists2, "Domain-only search should not match.");
  }

  @Test
  public void testExistsByEmailIgnoreCase_SimilarButDifferentEmails() {
    User user = new User();
    user.setEmail("testUser@example.com");
    user.setUsername("Test User2");
    userRepository.save(user);

    boolean exists = userRepository.existsByEmailIgnoreCase("testUser@different.com");

    assertFalse(exists, "Different domain should not match.");
  }

  @Test
  public void testExistsByEmailIgnoreCase_UnicodeEmail() {
    User user = new User();
    user.setEmail("tést@example.com");
    user.setUsername("Unicode User");
    userRepository.save(user);

    boolean exists = userRepository.existsByEmailIgnoreCase("tést@example.com");

    assertTrue(exists, "Unicode email should be found.");
  }
}
