package planettask.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
public class ReservationDTO implements Serializable {

    private Long reservationId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private UserDTO user;

    @Max(value = 3, message = "Limit of books in reservation is 3")
    private Set<BookDTO> books;

}
