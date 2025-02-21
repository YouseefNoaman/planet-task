package planettask.service;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    public static final int MAX_BOOKS_IN_RESERVATIONS = 3;

    @Cacheable(value = "reservation", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public List<ReservationDTO> findAll(final Pageable pageable) {
        final Page<Reservation> reservations = this.reservationRepository.findAll(pageable);
        return reservations.stream().map(reservation -> modelMapper.map(reservation, ReservationDTO.class)).toList();
    }

    @Cacheable(value = "reservation", key = "#reservationId")
    public ReservationDTO get(final Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("Reservation not found"));

        ReservationDTO reservationDTO = modelMapper.map(reservation, ReservationDTO.class);
        reservationDTO.setUser(modelMapper.map(reservation.getUser(), UserDTO.class));

        Set<BookDTO> bookDTOs = reservation.getBooks().stream()
            .map(book -> modelMapper.map(book, BookDTO.class))
            .collect(Collectors.toSet());
        reservationDTO.setBooks(bookDTOs);
        return reservationDTO;
    }

    public Long create(final ReservationDTO reservationDTO) {
        if (reservationDTO == null) {
            throw new IllegalArgumentException("ReservationDTO cannot be null");
        }

        Reservation reservation = modelMapper.map(reservationDTO, Reservation.class);
        return reservationRepository.save(reservation).getReservationId();
    }

    @CachePut(value = "reservation", key = "#reservationId")
    public ReservationDTO cancelReservation(Long reservationId) {
        // Fetch the reservation; throw exception if not found
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("Reservation not found"));

        // Check if the reservation is active
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new UnsupportedOperationException("Only active reservations can be canceled");
        }

        // Update the reservation status to CANCELED and save it
        reservation.setStatus(ReservationStatus.CANCELED);
        Reservation updatedReservation = reservationRepository.save(reservation);

        // Update available copies for each book associated with this reservation
        Set<Book> books = updatedReservation.getBooks();
        books.forEach(book -> book.setAvailableCopies(book.getAvailableCopies() + 1));
        bookRepository.saveAll(books);

        // Map the updated reservation to a DTO and return it,
        // so that the cache is updated with the latest state.
        return modelMapper.map(updatedReservation, ReservationDTO.class);
    }

    public ReservationDTO reserveBooks(Long userId, Set<Long> bookIds) throws Exception {

        if (bookIds.size() > MAX_BOOKS_IN_RESERVATIONS) {
            throw new Exception("Limit of books in reservation is " + MAX_BOOKS_IN_RESERVATIONS);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        Set<Book> books = new HashSet<>(3);
        for (Long bookId : bookIds) {
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found with id " + bookId));

            if (book.getAvailableCopies() <= 0) {
                throw new Exception("Book '" + book.getTitle() + "' is not available for reservation");
            }
            books.add(book);
        }

        for (Book book : books) {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
        }
        bookRepository.saveAll(books);

        Reservation reservation = Reservation.builder()
            .user(user)
            .books(books)
            .status(ReservationStatus.ACTIVE)
            .build();
        reservation = reservationRepository.save(reservation);

        ReservationDTO reservationDTO = modelMapper.map(reservation, ReservationDTO.class);
        Set<BookDTO> bookDTOs = books.stream()
            .map(book -> modelMapper.map(book, BookDTO.class))
            .collect(Collectors.toSet());
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        reservationDTO.setUser(userDTO);
        reservationDTO.setBooks(bookDTOs);

        return reservationDTO;
    }


    @Cacheable(value = "reservationsByUser", key = "#userId")
    public Set<ReservationDTO> getReservationsByUserId(Long userId) {
        Set<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream().map(reservation -> modelMapper.map(reservation, ReservationDTO.class)).collect(Collectors.toSet());
    }
}
