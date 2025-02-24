package planettask.repos;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import planettask.domain.Reservation;
import planettask.domain.User;
import planettask.model.ReservationStatus;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = Replace.ANY)
class ReservationRepositoryTest {

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private UserRepository userRepository;

  private Reservation activeReservation;
  private Reservation expiredReservation;
  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setUsername("John Doe");
    user.setEmail("John-Doe@gmail.com");
    userRepository.save(user);

    activeReservation = new Reservation();
    activeReservation.setUser(user);
    activeReservation.setStatus(ReservationStatus.ACTIVE);
    activeReservation.setDateCreated(OffsetDateTime.now().minusDays(5));
    reservationRepository.save(activeReservation);

    expiredReservation = new Reservation();
    expiredReservation.setUser(user);
    expiredReservation.setStatus(ReservationStatus.ACTIVE);
    expiredReservation.setDateCreated(OffsetDateTime.now().minusDays(10));
    reservationRepository.save(expiredReservation);
  }

  @Test
  void findByStatusAndDateCreatedBefore_ShouldReturnExpiredReservations() {
    // Given
    OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);

    // When
    Set<Reservation> expiredReservations = reservationRepository.findByStatusAndDateCreatedBefore(
        ReservationStatus.ACTIVE, sevenDaysAgo);

    // Then
    assertThat(expiredReservations).isNotEmpty();
    assertThat(expiredReservations).contains(expiredReservation);
  }

  @Test
  void findByStatusAndDateCreatedBefore_ShouldReturnEmpty_WhenNoReservationsMatch() {
    // Given
    OffsetDateTime oneDayAgo = OffsetDateTime.now().minusDays(1);

    // When
    Set<Reservation> expiredReservations = reservationRepository.findByStatusAndDateCreatedBefore(
        ReservationStatus.ACTIVE, oneDayAgo);

    // Then
    assertThat(expiredReservations).isEmpty();
  }

  @Test
  void findByUserId_ShouldReturnReservationsForUser() {
    // When
    Set<Reservation> userReservations = reservationRepository.findByUserId(user.getUserId());

    // Then
    assertThat(userReservations).hasSize(2);
    assertThat(userReservations).contains(activeReservation, expiredReservation);
  }

  @Test
  void save_ShouldCreateNewReservation() {
    // Given
    Reservation newReservation = new Reservation();
    newReservation.setUser(user);
    newReservation.setStatus(ReservationStatus.ACTIVE);
    newReservation.setDateCreated(OffsetDateTime.now());

    // When
    Reservation savedReservation = reservationRepository.save(newReservation);

    // Then
    assertThat(savedReservation.getReservationId()).isNotNull();
    assertThat(reservationRepository.findAll()).hasSize(3);
  }

  @Test
  void findAll_ShouldReturnAllReservations() {
    // When
    Set<Reservation> reservations = Set.copyOf(reservationRepository.findAll());

    // Then
    assertThat(reservations).hasSize(2);
  }

  @Test
  void findById_ShouldReturnReservation_WhenExists() {
    // When
    Optional<Reservation> foundReservation = reservationRepository.findById(activeReservation.getReservationId());

    // Then
    assertThat(foundReservation).isPresent();
    assertThat(foundReservation.get().getUser()).isEqualTo(user);
  }

  @Test
  void findById_ShouldReturnEmpty_WhenNotFound() {
    // When
    Optional<Reservation> foundReservation = reservationRepository.findById(999L);

    // Then
    assertThat(foundReservation).isEmpty();
  }
}

