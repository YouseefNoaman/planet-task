package planettask.repos;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import lombok.extern.slf4j.Slf4j;
import planettask.domain.User;

@DataJpaTest
@AutoConfigureTestEntityManager
@Slf4j
class UserRepositoryTest {

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
  void testExistsByEmailIgnoreCase_UserExists() {
    // Given

    // When
    boolean exists = userRepository.existsByEmailIgnoreCase("TEST@EXAMPLE.COM");

    // Then
    assertTrue(exists, "User should exist even if email case is different.");
  }

  @Test
  void testExistsByEmailIgnoreCase_UserDoesNotExist() {
    // When
    boolean exists = userRepository.existsByEmailIgnoreCase("notfound@example.com");

    // Then
    assertFalse(exists, "User should not exist in database.");
  }

  @Test
  void testFindById_UserExists() {
    // Given
    User savedUser = new User();
    savedUser.setEmail("found@example.com");
    savedUser.setUsername("Found User");
    userRepository.save(savedUser);
    // When
    Optional<User> foundUser = userRepository.findById(savedUser.getUserId());

    // Then
    assertTrue(foundUser.isPresent(), "User should be found by ID.");
    log.info("User email: {}", savedUser.getEmail());
    log.info("Found user email: {}", foundUser.get().getEmail());
    assertEquals(savedUser.getEmail(),  foundUser.get().getEmail(),"Emails should match.");
  }

  @Test
  void testFindById_UserDoesNotExist() {
    // When
    Optional<User> foundUser = userRepository.findById(999L);

    // Then
    assertFalse(foundUser.isPresent(), "No user should be found for a non-existing ID.");
  }

  @Test
  void testExistsByEmailIgnoreCase_EmptyEmail() {
    boolean exists = userRepository.existsByEmailIgnoreCase("");

    assertFalse(exists, "Empty email should not be found.");
  }

  @Test
  void testExistsByEmailIgnoreCase_NullEmail() {
    boolean exists = userRepository.existsByEmailIgnoreCase(null);

    assertFalse(exists, "Null email should not be found.");
  }

  @Test
  void testExistsByEmailIgnoreCase_WhitespaceEmail() {
    User existingUser = new User();
    existingUser.setEmail("trimmed@example.com");
    existingUser.setUsername("Trimmed User");
    userRepository.save(existingUser);

    boolean exists = userRepository.existsByEmailIgnoreCase("   trimmed@example.com   ");

    assertFalse(exists, "Whitespace should not match email.");
  }

  @Test
  void testExistsByEmailIgnoreCase_PartialEmail() {
    User partialUser = new User();
    partialUser.setEmail("partial@example.com");
    partialUser.setUsername("Partial User");
    userRepository.save(partialUser);

    boolean exists = userRepository.existsByEmailIgnoreCase("partial@");
    boolean exists2 = userRepository.existsByEmailIgnoreCase("example.com");

    assertFalse(exists, "Partial email should not be found.");
    assertFalse(exists2, "Domain-only search should not match.");
  }

  @Test
  void testExistsByEmailIgnoreCase_SimilarButDifferentEmails() {
    User existingUser = new User();
    existingUser.setEmail("testUser@example.com");
    existingUser.setUsername("Test User2");
    userRepository.save(existingUser);

    boolean exists = userRepository.existsByEmailIgnoreCase("testUser@different.com");

    assertFalse(exists, "Different domain should not match.");
  }

  @Test
  void testExistsByEmailIgnoreCase_UnicodeEmail() {
    User existingUser = new User();
    existingUser.setEmail("tést@example.com");
    existingUser.setUsername("Unicode User");
    userRepository.save(existingUser);

    boolean exists = userRepository.existsByEmailIgnoreCase("tést@example.com");

    assertTrue(exists, "Unicode email should be found.");
  }
}
