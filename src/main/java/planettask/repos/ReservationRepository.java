package planettask.repos;

import io.lettuce.core.dynamic.annotation.Param;
import java.time.OffsetDateTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import planettask.domain.Reservation;
import planettask.model.ReservationStatus;


public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  Set<Reservation> findByStatusAndDateCreatedBefore(
      @Param("status") ReservationStatus status,
      @Param("date") OffsetDateTime date
  );

  @Query("SELECT r FROM Reservation r WHERE r.user.userId = :userId")
  Set<Reservation> findByUserId(@Param("userId") Long userId);
}
