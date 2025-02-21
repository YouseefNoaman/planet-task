package planettask.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import planettask.domain.Book;
import planettask.domain.Reservation;
import planettask.domain.User;
import planettask.model.BookDTO;
import planettask.model.ReservationDTO;
import planettask.model.ReservationStatus;
import planettask.model.UserDTO;
import planettask.repos.BookRepository;
import planettask.repos.ReservationRepository;
import planettask.repos.UserRepository;
import planettask.util.NotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

  @Mock
  private ReservationRepository reservationRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private BookRepository bookRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private ReservationService reservationService;

  private Reservation reservation;
  private ReservationDTO reservationDTO;
  private User user;
  private UserDTO userDTO;
  private Book book;
  private BookDTO bookDTO;
  private Set<BookDTO> bookDTOs;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setUserId(1L);
    user.setUsername("John Doe");

    userDTO = new UserDTO();
    userDTO.setUserId(1L);
    userDTO.setUsername("John Doe");

    book = new Book();
    book.setBookId(1L);
    book.setTitle("Test Book");
    book.setAvailableCopies(2);
    book.setTotalCopies(5);
    book.setAuthor("John Doe");
    book.setIsbn("1234567890123");

    bookDTO = new BookDTO();
    bookDTO.setId(1L);
    bookDTO.setTitle("Test Book");
    bookDTO.setAvailableCopies(2);
    bookDTO.setTotalCopies(5);
    bookDTO.setAuthor("John Doe");
    bookDTO.setIsbn("1234567890123");

    bookDTOs = Set.of(bookDTO);

    reservation = new Reservation();
    reservation.setReservationId(1L);
    reservation.setUser(user);
    reservation.setBooks(Set.of(book));
    reservation.setStatus(ReservationStatus.ACTIVE);

    reservationDTO = new ReservationDTO();
    reservationDTO.setReservationId(1L);
    reservationDTO.setUser(userDTO);
    reservationDTO.setBooks(Set.of(bookDTO));
  }

  @Test
  void findAll_ShouldReturnListOfReservations() {
    Pageable pageable = PageRequest.of(0, 5);
    Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation));

    when(reservationRepository.findAll(pageable)).thenReturn(reservationPage);
    when(modelMapper.map(any(Reservation.class), eq(ReservationDTO.class))).thenReturn(reservationDTO);

    List<ReservationDTO> result = reservationService.findAll(pageable);

    assertEquals(1, result.size());
    assertEquals(1L, result.getFirst().getReservationId());
    verify(reservationRepository).findAll(pageable);
  }

  @Test
  void get_ShouldReturnReservationDTO_WhenReservationExists() {
    when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
    when(modelMapper.map(reservation, ReservationDTO.class)).thenReturn(reservationDTO);
    when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);
    when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(new BookDTO());

    ReservationDTO result = reservationService.get(1L);

    assertNotNull(result);
    assertEquals(1L, result.getReservationId());
    assertEquals(userDTO, result.getUser());
    assertEquals(bookDTOs.size(), result.getBooks().size());

    verify(reservationRepository, times(1)).findById(1L);
  }

  @Test
  void get_ShouldThrowNotFoundException_WhenReservationDoesNotExist() {
    when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> reservationService.get(1L));

    verify(reservationRepository, times(1)).findById(1L);
    verifyNoMoreInteractions(modelMapper);
  }
  @Test
  void create_ShouldReturnReservationId_WhenSuccessful() {
    when(modelMapper.map(reservationDTO, Reservation.class)).thenReturn(reservation);
    when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

    Long reservationId = reservationService.create(reservationDTO);

    assertNotNull(reservationId);
    assertEquals(1L, reservationId);
    verify(reservationRepository).save(any(Reservation.class));
  }

  @Test
  void create_ShouldThrowException_WhenReservationDTOIsNull() {
    assertThrows(IllegalArgumentException.class, () -> reservationService.create(null));

    verifyNoInteractions(reservationRepository);
  }

  @Test
  void cancelReservation_ShouldUpdateStatusAndIncreaseBookCopies() {
    when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
    when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
    when(modelMapper.map(any(Reservation.class), eq(ReservationDTO.class))).thenReturn(reservationDTO);

    reservationService.cancelReservation(1L);

    assertEquals(ReservationStatus.CANCELED, reservation.getStatus());
    assertEquals(3, book.getAvailableCopies());
    verify(reservationRepository).save(reservation);
    verify(bookRepository).saveAll(anySet());
  }

  @Test
  void cancelReservation_ShouldThrowException_WhenReservationNotActive() {
    reservation.setStatus(ReservationStatus.EXPIRED);
    when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

    assertThrows(UnsupportedOperationException.class, () -> reservationService.cancelReservation(1L));

    verify(reservationRepository, never()).save(any(Reservation.class));
  }

  @Test
  void reserveBooks_ShouldCreateReservation_WhenBooksAvailable() throws Exception {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
    when(modelMapper.map(any(Reservation.class), eq(ReservationDTO.class))).thenReturn(reservationDTO);
    when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);
    when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(bookDTO);

    ReservationDTO result = reservationService.reserveBooks(1L, Set.of(1L));

    assertNotNull(result);
    assertEquals(1, book.getAvailableCopies());
    verify(reservationRepository).save(any(Reservation.class));
    verify(bookRepository).saveAll(anySet());
  }

  @Test
  void reserveBooks_ShouldThrowException_WhenBookNotAvailable() {
    book.setAvailableCopies(0);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

    Exception exception = assertThrows(Exception.class, () -> reservationService.reserveBooks(1L, Set.of(1L)));
    assertEquals("Book 'Test Book' is not available for reservation", exception.getMessage());

    verify(reservationRepository, never()).save(any(Reservation.class));
  }

  @Test
  void reserveBooks_ShouldThrowException_WhenMoreThanMaxBooksReserved() {
    Set<Long> bookIds = Set.of(1L, 2L, 3L, 4L);
    Exception exception = assertThrows(Exception.class, () -> reservationService.reserveBooks(1L, bookIds));
    assertEquals("Limit of books in reservation is 3", exception.getMessage());

    verify(reservationRepository, never()).save(any(Reservation.class));
  }

  @Test
  void getReservationsByUserId_ShouldReturnReservations() {
    when(reservationRepository.findByUserId(1L)).thenReturn(Set.of(reservation));
    when(modelMapper.map(any(Reservation.class), eq(ReservationDTO.class))).thenReturn(reservationDTO);

    Set<ReservationDTO> result = reservationService.getReservationsByUserId(1L);

    assertEquals(1, result.size());
    verify(reservationRepository).findByUserId(1L);
  }
}

