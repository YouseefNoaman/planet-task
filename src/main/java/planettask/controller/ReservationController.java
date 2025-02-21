package planettask.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import planettask.model.ReservationDTO;
import planettask.service.ReservationService;


@RestController
@RequestMapping(value = "/api/v1/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class ReservationController {

  private final ReservationService reservationService;

  public ReservationController(final ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @GetMapping
  public ResponseEntity<List<ReservationDTO>> getAllReservations(
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "10") final int size,
      @RequestParam(defaultValue = "reservationId,asc") final String[] sort) {
    final Sort.Direction sortDirection = sort[1].equalsIgnoreCase("desc")
        ? Sort.Direction.DESC
        : Sort.Direction.ASC;
    final Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort[0]));

    return ResponseEntity.ok(this.reservationService.findAll(pageable));
  }

  @GetMapping("/{reservationId}")
  public ResponseEntity<ReservationDTO> getReservation(
      @PathVariable(name = "reservationId") final Long reservationId) {
    return ResponseEntity.ok(reservationService.get(reservationId));
  }

  @PostMapping("/{userId}")
  @ApiResponse(description = "create reservation given user ID and set of book IDs", responseCode = "201")
  public ResponseEntity<ReservationDTO> createReservation(@PathVariable Long userId,
      @RequestParam Set<Long> booksIds) throws Exception {
    return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.reserveBooks(userId, booksIds));
  }

  @PutMapping("/cancel/{reservationId}")
  public ResponseEntity<?> cancelReservation(
      @PathVariable(name = "reservationId") final Long reservationId) {
    reservationService.cancelReservation(reservationId);
    return ResponseEntity.accepted().body("Cancelled reservation with ID: " + reservationId);
  }

  @GetMapping("/user/{userId}")
  public Set<ReservationDTO> getReservationsByUser(@PathVariable Long userId) {
    return reservationService.getReservationsByUserId(userId);
  }

}
