package planettask.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import planettask.domain.Book;
import planettask.domain.Reservation;
import planettask.model.ReservationStatus;
import planettask.repos.BookRepository;
import planettask.repos.ReservationRepository;

class ReservationSchedulerTest {

  @Mock
  private ReservationRepository reservationRepository;

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private ReservationScheduler reservationScheduler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void expireOldReservations_ShouldUpdateStatusAndRestoreBooks() {
    OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(10);
    Reservation oldReservation = new Reservation();
    oldReservation.setStatus(ReservationStatus.ACTIVE);
    oldReservation.setDateCreated(sevenDaysAgo);

    Book book1 = new Book();
    book1.setAvailableCopies(2);
    Book book2 = new Book();
    book2.setAvailableCopies(1);

    Set<Book> books = new HashSet<>();
    books.add(book1);
    books.add(book2);

    oldReservation.setBooks(books);

    Set<Reservation> oldReservations = Set.of(oldReservation);

    when(reservationRepository.findByStatusAndDateCreatedBefore(eq(ReservationStatus.ACTIVE), any()))
        .thenReturn(oldReservations);

    reservationScheduler.expireOldReservations();

    assertEquals(ReservationStatus.EXPIRED, oldReservation.getStatus());

    // Books should have their available copies increased
    assertEquals(3, book1.getAvailableCopies()); // 2 → 3
    assertEquals(2, book2.getAvailableCopies()); // 1 → 2

    verify(bookRepository, times(1)).saveAll(books);
    verify(reservationRepository, times(1)).saveAll(oldReservations);
  }

  @Test
  void expireOldReservations_ShouldDoNothingIfNoOldReservations() {
    OffsetDateTime now = OffsetDateTime.now();
    Reservation oldReservation = new Reservation();
    oldReservation.setStatus(ReservationStatus.ACTIVE);
    oldReservation.setDateCreated(now);

    Book book1 = new Book();
    book1.setAvailableCopies(2);
    Book book2 = new Book();
    book2.setAvailableCopies(1);

    Set<Book> books = new HashSet<>();
    books.add(book1);
    books.add(book2);

    oldReservation.setBooks(books);
    Set<Reservation> oldReservations = Set.of(oldReservation);

    when(reservationRepository.findByStatusAndDateCreatedBefore(ReservationStatus.ACTIVE, now))
        .thenReturn(oldReservations);

    reservationScheduler.expireOldReservations();

    assertEquals(ReservationStatus.ACTIVE, oldReservation.getStatus());

    assertEquals(2, book1.getAvailableCopies()); // 2 → 3
    assertEquals(1, book2.getAvailableCopies()); // 1 → 2
  }

  @Test
  public void testReservationsExpired() {
    // Given
    Reservation reservation = new Reservation();
    reservation.setStatus(ReservationStatus.ACTIVE);
    reservation.setDateCreated(OffsetDateTime.now().minusDays(10));
    // Mock books
    Book book1 = new Book();
    book1.setAvailableCopies(2);
    Book book2 = new Book();
    book2.setAvailableCopies(1);

    Set<Book> books = new HashSet<>();
    books.add(book1);
    books.add(book2);

    reservation.setBooks(books);
    Set<Reservation> oldReservations = Set.of(reservation);
    when(reservationRepository.findByStatusAndDateCreatedBefore(any(), any())).thenReturn(oldReservations);
    // When
    reservationScheduler.expireOldReservations();
    // Then
    verify(reservationRepository).saveAll(oldReservations);
    verify(bookRepository).saveAll(any());
  }
  @Test
  public void testBookCopiesRestored() {
    // Given
    Reservation reservation = new Reservation();
    reservation.setStatus(ReservationStatus.ACTIVE);
    reservation.setDateCreated(OffsetDateTime.now().minusDays(10));
    Book book = new Book();
    book.setAvailableCopies(2);
    reservation.setBooks(Set.of(book));
    Set<Reservation> oldReservations = Set.of(reservation);
    when(reservationRepository.findByStatusAndDateCreatedBefore(any(), any())).thenReturn(oldReservations);
    // When
    reservationScheduler.expireOldReservations();
    // Then
    assertEquals(3, book.getAvailableCopies());
  }
  @Test
  public void testReservationStatusUpdated() {
    // Given
    Reservation reservation = new Reservation();
    reservation.setStatus(ReservationStatus.ACTIVE);
    reservation.setDateCreated(OffsetDateTime.now().minusDays(10));
    // Mock books
    Book book1 = new Book();
    book1.setAvailableCopies(2);
    Book book2 = new Book();
    book2.setAvailableCopies(1);

    Set<Book> books = new HashSet<>();
    books.add(book1);
    books.add(book2);

    reservation.setBooks(books);
    Set<Reservation> oldReservations = Set.of(reservation);
    when(reservationRepository.findByStatusAndDateCreatedBefore(any(), any())).thenReturn(oldReservations);
    // When
    reservationScheduler.expireOldReservations();
    // Then
    assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
  }

}
