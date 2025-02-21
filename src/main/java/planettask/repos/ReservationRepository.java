package planettask.repos;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Query;
import planettask.domain.Reservation;
import planettask.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Set;


public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.dateCreated < :date")
    Set<Reservation> findByStatusAndDateCreatedBefore(
        @Param("status") ReservationStatus status,
        @Param("date") OffsetDateTime date
    );

    @Query("SELECT r FROM Reservation r WHERE r.user.userId = :userId")
    Set<Reservation> findByUserId(@Param("userId") Long userId);
}
