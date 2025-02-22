package planettask.service;

import java.time.OffsetDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import planettask.domain.Book;
import planettask.domain.Reservation;
import planettask.model.ReservationStatus;
import planettask.repos.BookRepository;
import planettask.repos.ReservationRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

  public static final int DAYS_TO_EXPIRE = 7;
  ReservationRepository reservationRepository;
  BookRepository bookRepository;

  @Scheduled(cron = "0 0 0 * * ?") // Runs every day at midnight
  public void expireOldReservations() {
    OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(DAYS_TO_EXPIRE).withHour(0)
        .withMinute(0);
    log.info("Looking for reservations older than {} ", sevenDaysAgo);

    // Find reservations that are ACTIVE and older than 7 days
    Set<Reservation> oldReservations = reservationRepository.findByStatusAndDateCreatedBefore(
        ReservationStatus.ACTIVE, sevenDaysAgo);
    if (oldReservations.isEmpty()) {
      log.info("No old reservations to expire.");
      return;
    }

    for (Reservation reservation : oldReservations) {
      reservation.setStatus(ReservationStatus.EXPIRED);

      // Restore book copies
      Set<Book> books = reservation.getBooks();
      books.forEach(book -> book.setAvailableCopies(book.getAvailableCopies() + 1));
      bookRepository.saveAll(books);
    }
    reservationRepository.saveAll(oldReservations);
    log.info("Expired {} reservation(s).", oldReservations.size());
  }

}
